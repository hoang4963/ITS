package com.its.econtract.notification;

import com.google.common.collect.Lists;
import com.its.econtract.entity.ECDocumentAssignee;
import com.its.econtract.entity.ECDocumentConversation;
import com.its.econtract.entity.ECSendEmail;
import com.its.econtract.entity.enums.ECNotiType;
import com.its.econtract.entity.enums.ECNotificationType;
import com.its.econtract.exceptions.ECBusinessException;
import com.its.econtract.repository.ECDocumentAssigneeRepository;
import com.its.econtract.repository.ECDocumentConversationRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;

import javax.mail.internet.MimeMessage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@Log4j2
public class EmailSendNotification implements ISendNotification {

    private JavaMailSender emailSender;
    private EmailSendNotificationBuilder builder;
    private ECDocumentConversationRepository documentConversationRepository;


    private EmailSendNotification(EmailSendNotificationBuilder builder) {
        this.builder = builder;
        this.emailSender = buildEmailSender(builder.setting);
    }

    private JavaMailSender buildEmailSender(ECSendEmail setting) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(setting.getHost());
        mailSender.setPort(setting.getPort());
        mailSender.setUsername(setting.getAddress());
        mailSender.setPassword(setting.getPassword());

        // Need to review database
        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", setting.getProtocol());
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", setting.getSsl() == 1);
        props.put("mail.smtp.ssl.enable", setting.getSsl() == 1);
        return mailSender;
    }

    @Override
    public void setConversationRepository(ECDocumentConversationRepository documentConversationRepository) {
        this.documentConversationRepository = documentConversationRepository;
    }

    @Override
    public int sendMsg() {
        try {
            log.info("EmailSendNotification sendMsg |=============>>");
            List<ECDocumentConversation> conversations = Lists.newArrayList();
            if (builder.isCheckAcc) {
                for (Map.Entry<String, String> entry : builder.contentsAcc.entrySet()) {
                    boolean result = sendEmail(new String[]{entry.getKey()}, builder.subject, entry.getValue());
                    log.info("Result : {}", result);
                    conversations.add(new ECDocumentConversation(builder.companyId,
                            builder.documentId, builder.templateId, -1,
                            ECNotificationType.EMAIL.getValue(), entry.getValue(), result ? 1 : 2, -1));
                }
            } else {
                if (!builder.isSaveContent) {
                    for (Map.Entry<ECDocumentAssignee, String> entry : builder.contents.entrySet()) {
                        String content = entry.getValue();
                        boolean result = sendEmail(new String[]{entry.getKey().getEmail()}, builder.subject, content);
                        log.info("Result send email: {}", result);
                        log.info("Save email sending log in database");
                        conversations.add(new ECDocumentConversation(builder.companyId,
                                builder.documentId, builder.templateId, ECNotiType.EMAIL.getValue().intValue(),
                                ECNotificationType.EMAIL.getValue(), content, result ? 1 : 2, entry.getKey().getId()));
                        if (result && entry.getKey().getState() == 0) {
                            builder.ecDocumentAssigneeRepository.updateState(entry.getKey().getId(), 1);
                        }
                    }
                } else {
                    List<String> cc = new ArrayList<>();
                    List<ECDocumentAssignee> assignees = new ArrayList<>();
                    for (Map.Entry<ECDocumentAssignee, String> entry : builder.contents.entrySet()) {
                        cc.add(entry.getKey().getEmail());
                        assignees.add(entry.getKey());
                    }
                    boolean result = sendEmail(cc.toArray(new String[0]), builder.subject, builder.singleContent);
                    assignees.forEach(item -> conversations.add(new ECDocumentConversation(builder.companyId,
                            builder.documentId, builder.templateId, ECNotiType.EMAIL.getValue().intValue(),
                            ECNotificationType.EMAIL.getValue(),
                            builder.singleContent, result ? 1 : 2, item.getId())));
                }
            }

            this.storeData(conversations);
            return 1;
        } catch (Exception e) {
            throw new ECBusinessException("Send Email error");
        }
    }

    @Override
    public int reSendMsg() {
        log.info("EmailSendNotification reSendMsg |=============>>");
        List<ECDocumentConversation> conversations = Lists.newArrayList();
        for (Map.Entry<ECDocumentAssignee, String> entry : builder.contents.entrySet()) {
            String content = entry.getValue();
            boolean result = sendEmail(new String[]{entry.getKey().getEmail()}, builder.subject, content);
            log.info("Result resend email: {}", result);
            log.info("Save email re sending log in database");
            builder.ecDocumentConversation.setStatus(result ? 1 : 2);
            if (result && entry.getKey().getState() == 0) {
                builder.ecDocumentAssigneeRepository.updateState(entry.getKey().getId(), 1);
            }
            builder.ecDocumentConversationRepository.save(builder.ecDocumentConversation);
        }
        this.storeData(conversations);
        return 1;
    }

    private boolean sendEmail(String[] cc, String subject, String content) {
        try {
            log.info("===================== Send email ======================>");
            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(cc);
            helper.setSubject(subject);
            helper.setText(content, true);
            helper.setFrom(builder.setting.getAddress());
            emailSender.send(message);
            log.info("====================== End email ======================>");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void storeData(List<ECDocumentConversation> conversations) {
        this.documentConversationRepository.saveAll(conversations);
    }

    public static class EmailSendNotificationBuilder implements IBuilder<EmailSendNotification> {
        //Setting
        protected ECSendEmail setting;

        // Content send emails
        protected String[] bcc;
        protected String subject;
        protected boolean isSaveContent = false;
        protected Map<ECDocumentAssignee, String> contents;
        protected Map<String, String> contentsAcc;
        protected String singleContent;
        protected int companyId = -1;
        protected int documentId = -1;
        protected int templateId = -1;
        protected boolean isCheckAcc = false;
        protected ECDocumentConversation ecDocumentConversation;
        protected ECDocumentAssigneeRepository ecDocumentAssigneeRepository;
        protected ECDocumentConversationRepository ecDocumentConversationRepository;

        public EmailSendNotificationBuilder withCompany(int companyId) {
            this.companyId = companyId;
            return this;
        }

        public EmailSendNotificationBuilder withDocumentConversationRepository(ECDocumentConversationRepository ecDocumentConversationRepository) {
            this.ecDocumentConversationRepository = ecDocumentConversationRepository;
            return this;
        }

        public EmailSendNotificationBuilder withDocumentAssignee(ECDocumentAssigneeRepository ecDocumentAssigneeRepository) {
            this.ecDocumentAssigneeRepository = ecDocumentAssigneeRepository;
            return this;
        }

        public EmailSendNotificationBuilder withConversation(ECDocumentConversation ecDocumentConversation) {
            this.ecDocumentConversation = ecDocumentConversation;
            return this;
        }

        public EmailSendNotificationBuilder withTemplate(int templateId) {
            this.templateId = templateId;
            return this;
        }

        public EmailSendNotificationBuilder withDocument(int documentId) {
            this.documentId = documentId;
            return this;
        }

        public EmailSendNotificationBuilder withEmailConfiguration(ECSendEmail setting) {
            this.setting = setting;
            return this;
        }


        public EmailSendNotificationBuilder withSubject(String subject) {
            this.subject = subject;
            return this;
        }

        public EmailSendNotificationBuilder withContents(Map<ECDocumentAssignee, String> content) {
            this.contents = content;
            return this;
        }

        public EmailSendNotificationBuilder withContentsAcc(Map<String, String> content) {
            this.contentsAcc = content;
            return this;
        }

        public EmailSendNotificationBuilder isSameContent(boolean isSaveContent) {
            this.isSaveContent = isSaveContent;
            return this;
        }

        public EmailSendNotificationBuilder isCheckAcc(boolean isSaveContentAcc) {
            this.isCheckAcc = isSaveContentAcc;
            return this;
        }

        public EmailSendNotificationBuilder withSingleContent(String singleContent) {
            this.singleContent = singleContent;
            return this;
        }

        @Override
        public EmailSendNotification build() {
            return new EmailSendNotification(this);
        }
    }
}
