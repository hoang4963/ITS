package com.its.econtract.notification.sms;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.its.econtract.notification.IBuilder;
import com.its.econtract.entity.ECDocumentAssignee;
import com.its.econtract.entity.ECDocumentConversation;
import com.its.econtract.entity.enums.ECNotiType;
import com.its.econtract.entity.enums.ECNotificationType;
import com.its.econtract.repository.ECDocumentAssigneeRepository;
import com.its.econtract.repository.ECDocumentConversationRepository;
import lombok.extern.log4j.Log4j2;
import okhttp3.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log4j2
public class VMGSender implements ISmsSender {

    private final Gson gson;
    private final VMGSenderBuilder builder;


    @Override
    public Object sendSms() {
        this.setup();
        return 1;
    }

    private VMGSender(VMGSenderBuilder builder) {
        this.builder = builder;
        this.gson = new Gson();
    }

    private void setup() {
        OkHttpClient client = new OkHttpClient();
        List<ECDocumentConversation> conversations = Lists.newArrayList();
        for (Map.Entry<ECDocumentAssignee, Map<String, String>> entry : builder.content.entrySet()) {
            Map<String, Object> map = new HashMap<>();
            map.put("to", entry.getKey().getPhone());
            map.put("type", 1);
            map.put("from", builder.username);
            map.put("message", entry.getValue().get("content"));
            map.put("telco", "");
            map.put("scheduled", "");
            boolean result = broadcastMsg(client, map);
            conversations.add(new ECDocumentConversation(builder.companyId,
                    builder.documentId, builder.templateId,
                    ECNotiType.SMS.getValue().intValue(),
                    ECNotificationType.SMS.getValue(), entry.getValue().get("content"), result ? 1 : 2,
                    Integer.parseInt(entry.getValue().get("assign_id"))));
            if (result && entry.getKey().getState() == 0){
                builder.ecDocumentAssigneeRepository.updateState(entry.getKey().getId(), 1);
            }
        }
        builder.documentConversationRepository.saveAll(conversations);
    }

    private boolean broadcastMsg(OkHttpClient httpClient, Map<String, Object> sender) {
        try {
            RequestBody body = RequestBody.create(gson.toJson(sender), MediaType.parse("application/json; charset=utf-8"));
            Request request = new Request.Builder()
                    .url(this.builder.url)
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("token", this.builder.password)
                    .build();
            Response response = httpClient.newCall(request).execute();
            return response.isSuccessful();
        } catch (IOException e) {
            log.error("VMGSender broadcastMsg cause: ", e);
        }
        return false;
    }

    public static class VMGSenderBuilder implements IBuilder {
        protected String url;
        protected String username;
        protected String password;
        protected int companyId;
        protected int templateId;
        protected int documentId;
        protected ECDocumentConversationRepository documentConversationRepository;
        protected Map<ECDocumentAssignee, Map<String, String>> content;
        protected ECDocumentAssigneeRepository ecDocumentAssigneeRepository;

        public VMGSenderBuilder withECDocumentAssigneeRepository(ECDocumentAssigneeRepository ecDocumentAssigneeRepository) {
            this.ecDocumentAssigneeRepository = ecDocumentAssigneeRepository;
            return this;
        }
        public VMGSenderBuilder setStore(ECDocumentConversationRepository documentConversationRepository) {
            this.documentConversationRepository = documentConversationRepository;
            return this;
        }

        public VMGSenderBuilder withUrl(String url) {
            this.url = url;
            return this;
        }

        public VMGSenderBuilder withUsername(String username) {
            this.username = username;
            return this;
        }

        public VMGSenderBuilder withPassword(String password) {
            this.password = password;
            return this;
        }

        public VMGSenderBuilder withContent(Map<ECDocumentAssignee, Map<String, String>> content) {
            this.content = content;
            return this;
        }

        public VMGSenderBuilder withTemplate(int templateId) {
            this.templateId = templateId;
            return this;
        }

        public VMGSenderBuilder withCompany(int companyId) {
            this.companyId = companyId;
            return this;
        }
        public VMGSenderBuilder withDocument(int documentId) {
            this.documentId = documentId;
            return this;
        }

        @Override
        public VMGSender build() {
            return new VMGSender(this);
        }
    }
}
