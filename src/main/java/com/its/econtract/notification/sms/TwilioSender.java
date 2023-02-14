package com.its.econtract.notification.sms;

import com.google.common.collect.Lists;
import com.its.econtract.notification.IBuilder;
import com.its.econtract.entity.ECDocumentAssignee;
import com.its.econtract.entity.ECDocumentConversation;
import com.its.econtract.entity.enums.ECNotiType;
import com.its.econtract.entity.enums.ECNotificationType;
import com.its.econtract.repository.ECDocumentAssigneeRepository;
import com.its.econtract.repository.ECDocumentConversationRepository;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.rest.api.v2010.account.MessageCreator;
import com.twilio.type.PhoneNumber;
import lombok.extern.log4j.Log4j2;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Log4j2
public class TwilioSender implements ISmsSender {

    private final TwilioSenderBuilder builder;

    @Override
    public Object sendSms() {
        this.setup();
        return 1;
    }

    private TwilioSender(TwilioSenderBuilder builder) {
        this.builder = builder;
        Twilio.init(builder.account, builder.password);
    }

    private void setup() {
        log.info("SmsSendNotification sendMsg |=============>>");
        List<ECDocumentConversation> conversations = Lists.newArrayList();
        for (Map.Entry<ECDocumentAssignee, Map<String, String>> entry : builder.content.entrySet()) {
            String message = String.valueOf(entry.getValue().get("content"));
            boolean result = sendSms(entry);
            conversations.add(new ECDocumentConversation(builder.companyId,
                    builder.documentId, builder.templateId,
                    ECNotiType.SMS.getValue().intValue(),
                    ECNotificationType.SMS.getValue(), message, result ? 1 : 2,
                    Integer.parseInt(entry.getValue().get("assign_id"))));
            if (result && entry.getKey().getState() == 0) {
                builder.ecDocumentAssigneeRepository.updateState(entry.getKey().getId(), 1);
            }
        }
        this.storeData(conversations);
    }

    private void storeData(List<ECDocumentConversation> conversations) {
        builder.documentConversationRepository.saveAll(conversations);
    }

    private boolean sendSms(Map.Entry<ECDocumentAssignee, Map<String, String>> entry) {
        try {
            PhoneNumber to = new PhoneNumber(convertPhoneNumber(entry.getKey().getPhone()));
            PhoneNumber from = new PhoneNumber(builder.username);
            String message = String.valueOf(entry.getValue().get("content"));
            MessageCreator creator = Message.creator(to, from, message);
            creator.create();
            return true;
        } catch (Exception e) {
            log.error("TwilioSender sendSms cause", e);
        }
        return false;
    }

    private String convertPhoneNumber(String phoneNumber) {
        if (isPhoneNumberValid(phoneNumber)) {
            return "+84" + phoneNumber.replaceFirst("^0+", "");
        }
        return phoneNumber;
    }

    private boolean isPhoneNumberValid(String phoneNumber) {
        String regex = "^0[0-9]+$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(phoneNumber);
        return matcher.matches();
    }

    public static class TwilioSenderBuilder implements IBuilder {
        protected String username;
        protected String account;
        protected String password;
        protected int companyId;
        protected int documentId;
        protected int templateId;
        protected Map<ECDocumentAssignee, Map<String, String>> content;
        protected ECDocumentConversationRepository documentConversationRepository;
        protected ECDocumentAssigneeRepository ecDocumentAssigneeRepository;

        public TwilioSenderBuilder withECDocumentAssigneeRepository(ECDocumentAssigneeRepository ecDocumentAssigneeRepository) {
            this.ecDocumentAssigneeRepository = ecDocumentAssigneeRepository;
            return this;
        }

        public TwilioSenderBuilder withECDocumentConversationRepository(ECDocumentConversationRepository documentConversationRepository) {
            this.documentConversationRepository = documentConversationRepository;
            return this;
        }

        public TwilioSenderBuilder withUsername(String username) {
            this.username = username;
            return this;
        }

        public TwilioSenderBuilder withAccount(String account) {
            this.account = account;
            return this;
        }

        public TwilioSenderBuilder withPassword(String password) {
            this.password = password;
            return this;
        }

        public TwilioSenderBuilder withContent(Map<ECDocumentAssignee, Map<String, String>> content) {
            this.content = content;
            return this;
        }

        public TwilioSenderBuilder withCompany(int companyId) {
            this.companyId = companyId;
            return this;
        }

        public TwilioSenderBuilder withTemplate(int templateId) {
            this.templateId = templateId;
            return this;
        }

        public TwilioSenderBuilder withDocument(int documentId) {
            this.documentId = documentId;
            return this;
        }

        @Override
        public TwilioSender build() {
            return new TwilioSender(this);
        }
    }
}
