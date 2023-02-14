package com.its.econtract.notification;

import com.its.econtract.notification.sms.ISmsSender;
import com.its.econtract.notification.sms.TwilioSender;
import com.its.econtract.notification.sms.VMGSender;
import com.its.econtract.notification.sms.VTPSender;
import com.its.econtract.entity.ECDocumentAssignee;
import com.its.econtract.entity.ECSendSms;
import com.its.econtract.repository.ECDocumentAssigneeRepository;
import com.its.econtract.repository.ECDocumentConversationRepository;
import java.util.Map;

public class SmsSendNotification implements ISendNotification {

    protected SmsSendNotificationBuilder builder;
    protected ECDocumentConversationRepository documentConversationRepository;

    private SmsSendNotification(SmsSendNotificationBuilder smsSendNotificationBuilder) {
        this.builder = smsSendNotificationBuilder;
    }

    private ISmsSender buildSender(SmsSendNotificationBuilder builder) {
        switch (builder.smsSetting.getProvider()) {
            case "VMG":
                return new VMGSender.VMGSenderBuilder()
                        .setStore(documentConversationRepository)
                        .withPassword(builder.smsSetting.getPassword())
                        .withUrl(builder.smsSetting.getUrl())
                        .withUsername(builder.smsSetting.getBrandname())
                        .withContent(builder.contents)
                        .withTemplate(builder.templateId)
                        .withCompany(builder.companyId)
                        .withDocument(builder.documentId)
                        .withECDocumentAssigneeRepository(builder.ecDocumentAssigneeRepository)
                        .build();
            case "TWILIO":
                return new TwilioSender.TwilioSenderBuilder()
                        .withECDocumentConversationRepository(documentConversationRepository)
                        .withContent(builder.contents)
                        .withTemplate(builder.templateId)
                        .withCompany(builder.companyId)
                        .withDocument(builder.documentId)
                        .withAccount(builder.smsSetting.getAccount())
                        .withPassword(builder.smsSetting.getPassword())
                        .withUsername(builder.smsSetting.getBrandname())
                        .withECDocumentAssigneeRepository(builder.ecDocumentAssigneeRepository)
                        .build();
            case "VTP":
                return new VTPSender.VTPSenderBuilder()
                        .withECDocumentConversationRepository(documentConversationRepository)
                        .withContent(builder.contents)
                        .withTemplate(builder.templateId)
                        .withCompany(builder.companyId)
                        .withDocument(builder.documentId)
                        .withUrl(builder.smsSetting.getUrl())
                        .withAccount(builder.smsSetting.getAccount())
                        .withPassword(builder.smsSetting.getPassword())
                        .withCPCode(builder.cpCode)
                        .withUsername(builder.smsSetting.getBrandname())
                        .withECDocumentAssigneeRepository(builder.ecDocumentAssigneeRepository)
                        .build();
            case "NEO":

            default:
                throw new UnsupportedOperationException("Not support this method");
        }
    }

    @Override
    public void setConversationRepository(ECDocumentConversationRepository documentConversationRepository) {
        this.documentConversationRepository = documentConversationRepository;
    }

    @Override
    public int sendMsg() {
        buildSender(this.builder).sendSms();
        return 0;
    }

    @Override
    public int reSendMsg() {
        return 0;
    }

    public static class SmsSendNotificationBuilder implements IBuilder<SmsSendNotification> {

        //Setting
        protected ECSendSms smsSetting;

        // Content send emails
        protected Map<ECDocumentAssignee, Map<String, String>> contents;
        protected Map<String, String> contentsAcc;
        protected ECDocumentAssigneeRepository ecDocumentAssigneeRepository;
        protected int companyId = -1;
        protected int documentId = -1;
        protected int templateId = -1;
        protected String cpCode;
        public SmsSendNotificationBuilder withCompany(int companyId) {
            this.companyId = companyId;
            return this;
        }
        public SmsSendNotificationBuilder withCPCode(String cpCode) {
            this.cpCode = cpCode;
            return this;
        }

        public SmsSendNotificationBuilder withECDocumentAssigneeRepository(ECDocumentAssigneeRepository ecDocumentAssigneeRepository) {
            this.ecDocumentAssigneeRepository = ecDocumentAssigneeRepository;
            return this;
        }

        public SmsSendNotificationBuilder withTemplate(int templateId) {
            this.templateId = templateId;
            return this;
        }

        public SmsSendNotificationBuilder withDocument(int documentId) {
            this.documentId = documentId;
            return this;
        }

        public SmsSendNotificationBuilder withSmsConfiguration(ECSendSms smsSetting) {
            this.smsSetting = smsSetting;
            return this;
        }

        public SmsSendNotificationBuilder withSmsContents(Map<ECDocumentAssignee, Map<String, String>> contents) {
            this.contents = contents;
            return this;
        }
        public SmsSendNotificationBuilder withSmsContentsAcc(Map<String, String> contents) {
            this.contentsAcc = contents;
            return this;
        }

        @Override
        public SmsSendNotification build() {
            return new SmsSendNotification(this);
        }
    }
}
