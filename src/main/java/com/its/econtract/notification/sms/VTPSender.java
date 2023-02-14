package com.its.econtract.notification.sms;

import com.google.common.collect.Lists;
import com.its.econtract.notification.IBuilder;
import com.its.econtract.entity.ECDocumentAssignee;
import com.its.econtract.entity.ECDocumentConversation;
import com.its.econtract.entity.enums.ECNotiType;
import com.its.econtract.entity.enums.ECNotificationType;
import com.its.econtract.repository.ECDocumentAssigneeRepository;
import com.its.econtract.repository.ECDocumentConversationRepository;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.httpclient.methods.PostMethod;
import utils.Protocol;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Log4j2
public class VTPSender implements ISmsSender {
    private final VTPSenderBuilder builder;

    @Override
    public Object sendSms() {
        this.setup();
        return 1;
    }

    private VTPSender(VTPSenderBuilder builder) {
        this.builder = builder;
    }

    private void setup() {
        log.info("SmsSendNotification sendMsg vtp |=============>>");
        List<ECDocumentConversation> conversations = Lists.newArrayList();
        for (Map.Entry<ECDocumentAssignee, Map<String, String>> entry : builder.content.entrySet()) {
            String message = String.valueOf(entry.getValue().get("content"));
            boolean result = sendSms(entry);
            conversations.add(new ECDocumentConversation(builder.companyId,
                    builder.documentId, builder.templateId,
                    ECNotiType.SMS.getValue().intValue(),
                    ECNotificationType.SMS.getValue(), message, result ? 1 : 2,
                    Integer.parseInt(entry.getValue().get("assign_id"))));
            if (result && entry.getKey().getState() == 0){
                builder.ecDocumentAssigneeRepository.updateState(entry.getKey().getId(), 1);
            }
        }
        log.info("End sendMsg");
        this.storeData(conversations);
    }

    private void storeData(List<ECDocumentConversation> conversations) {
        builder.documentConversationRepository.saveAll(conversations);
    }

    private boolean sendSms(Map.Entry<ECDocumentAssignee, Map<String, String>> entry) {
        boolean result = false;
        long startTime = System.currentTimeMillis();

        // get content
        String message = String.valueOf(entry.getValue().get("content"));

        String request = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:impl=\"http://impl.bulkSms.ws/\">"
                + "<soapenv:Header/>"
                + "<soapenv:Body>"
                + "<impl:wsCpMt>"
                + "<User>" + builder.account + "</User>"
                + "<Password>" + builder.password + "</Password>"
                + "<CPCode>" + builder.cpCode + "</CPCode>"
                + "<RequestID>" + "1" + "</RequestID>"
                + "<UserID>" + convertPhoneNumber(entry.getKey().getPhone()) + "</UserID>"
                + "<ReceiverID>" + convertPhoneNumber(entry.getKey().getPhone()) + "</ReceiverID>"
                + "<ServiceID>" + builder.username + "</ServiceID>"
                + "<CommandCode>" + "bulksms" + "</CommandCode>"
                + "<Content>" + message + "</Content>"
                + "<ContentType>" + "0" + "</ContentType>"
                + "</impl:wsCpMt>"
                + "</soapenv:Body>"
                + "</soapenv:Envelope>";

        PostMethod post = null;
        try {
            Protocol protocol = new Protocol(builder.url);
            HttpClient httpclient = new HttpClient();
            HttpConnectionManager conMgr = httpclient.getHttpConnectionManager();
            HttpConnectionManagerParams conPars = conMgr.getParams();
            conPars.setConnectionTimeout(20000);
            conPars.setSoTimeout(60000);
            post = new PostMethod(protocol.getUrl());

            RequestEntity entity = new StringRequestEntity(request, "text/xml", "UTF-8");
            post.setRequestEntity(entity);
            post.setRequestHeader("SOAPAction", "#wsCpMt");
            httpclient.executeMethod(post);
            InputStream is = post.getResponseBodyAsStream();
            String response = null;
            if (is != null) {
                response = getStringFromInputStream(is);
            }
            log.info("Call sendMT response: " + response);

            if (response != null && !response.equals("")) {
                if (response.contains("<result>")) {
                    int start = response.indexOf("<result>") + "<result>".length();
                    int end = response.lastIndexOf("</result>");
                    String responseCode = response.substring(start, end);
                    if (responseCode.equalsIgnoreCase("1")) {
                        result = true; //call success
                    }
                }
            }
        } catch (Exception e) {
            log.error(e);
        } finally {
            if (post != null) {
                post.releaseConnection();
            }
        }

        log.info("Finish sendMT in " + (System.currentTimeMillis() - startTime) + " ms");
        return result;

    }

    private static String getStringFromInputStream(InputStream is) {
        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();

        String line;
        try {
            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            log.error(e);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    log.error(e);
                }
            }
        }

        return sb.toString();
    }

    private String convertPhoneNumber(String phoneNumber) {
        if (isPhoneNumberValid(phoneNumber)) {
            return "84" + phoneNumber.replaceFirst("^0+", "");
        }
        return phoneNumber;
    }

    private boolean isPhoneNumberValid(String phoneNumber) {
        String regex = "^0[0-9]+$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(phoneNumber);
        return matcher.matches();
    }


    public static class VTPSenderBuilder implements IBuilder{
        protected String username;
        protected String account;
        protected String password;
        protected String url;
        protected int companyId;
        protected int documentId;
        protected int templateId;
        protected Map<ECDocumentAssignee, Map<String, String>> content;
        protected ECDocumentConversationRepository documentConversationRepository;
        protected ECDocumentAssigneeRepository ecDocumentAssigneeRepository;
        protected String cpCode;

        public VTPSenderBuilder withECDocumentConversationRepository(ECDocumentConversationRepository documentConversationRepository) {
            this.documentConversationRepository = documentConversationRepository;
            return this;
        }
        public VTPSenderBuilder withECDocumentAssigneeRepository(ECDocumentAssigneeRepository ecDocumentAssigneeRepository) {
            this.ecDocumentAssigneeRepository = ecDocumentAssigneeRepository;
            return this;
        }

        public VTPSenderBuilder withUsername(String username) {
            this.username = username;
            return this;
        }

        public VTPSenderBuilder withUrl(String url) {
            this.url = url;
            return this;
        }

        public VTPSenderBuilder withAccount(String account) {
            this.account = account;
            return this;
        }

        public VTPSenderBuilder withPassword(String password) {
            this.password = password;
            return this;
        }

        public VTPSenderBuilder withCPCode(String cpCode) {
            this.cpCode = cpCode;
            return this;
        }

        public VTPSenderBuilder withContent(Map<ECDocumentAssignee, Map<String, String>> content) {
            this.content = content;
            return this;
        }

        public VTPSenderBuilder withCompany(int companyId) {
            this.companyId = companyId;
            return this;
        }

        public VTPSenderBuilder withTemplate(int templateId) {
            this.templateId = templateId;
            return this;
        }

        public VTPSenderBuilder withDocument(int documentId) {
            this.documentId = documentId;
            return this;
        }

        @Override
        public VTPSender build() {
            return new VTPSender(this);
        }
    }
}
