package com.its.econtract.facade;


import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.its.econtract.notification.EmailSendNotification;
import com.its.econtract.notification.ISendNotification;
import com.its.econtract.notification.SmsSendNotification;
import com.its.econtract.controllers.request.NotificationAccRequest;
import com.its.econtract.controllers.request.NotificationCompleteRequest;
import com.its.econtract.controllers.request.NotificationReSendMsgRequest;
import com.its.econtract.controllers.request.NotificationRequest;
import com.its.econtract.dto.EmailTemplateDto;
import com.its.econtract.entity.*;
import com.its.econtract.entity.enums.*;
import com.its.econtract.exceptions.ECBusinessException;
import com.its.econtract.utils.ECDateUtils;
import com.its.econtract.repository.*;
import com.its.econtract.utils.EmailTemplate;
import com.its.econtract.utils.StringUtil;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import javax.mail.MessagingException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Log4j2
@Component
public class ECNotificationFacade {

    @Autowired
    private ECDocumentRepository documentRepository;

    @Autowired
    private ECSendEmailRepository sendEmailRepository;

    @Autowired
    private NotificationTemplate notificationTemplate;

    @Autowired
    private ECSendSmsRepository sendSmsRepository;

    @Autowired
    private ECDocumentAssigneeRepository assigneeRepository;

    @Autowired
    private ECDocumentConversationRepository documentConversationRepository;

    @Autowired
    private SpringTemplateEngine templateEngine;

    @Value("${sign.url}")
    private String signUrl;

    @Autowired
    private ECDocumentPartnerRepository partnerRepository;

    @Autowired
    private ECConversationTemplateRepository ecConversationTemplateRepository;

    @Autowired
    private ECDocumentTypeRepository ecDocumentTypeRepository;

    @Value("${vtp.cpCode}")
    private String cpCode;

    public CompletableFuture<Integer> sendNotifyComplete(NotificationCompleteRequest notifiComplete) {
        Optional<ECDocuments> ecDocument = documentRepository.findById(notifiComplete.getDocumentId());
        if (!ecDocument.isPresent()) {
            throw new ECBusinessException("Not found the document in the system", HttpStatus.NOT_FOUND);
        }
        // lưu thời gian hoàn thành
        Date finishTime = ECDateUtils.currentTime();
        ecDocument.get().setFinishedDate(finishTime);
        Map<String, Object> exts = Maps.newConcurrentMap();
        exts.put("ten_tai_lieu", ecDocument.get().getName());
        exts.put("han_tai_lieu", ecDocument.get().getExpiredDate());
        exts.put("thoi_gian_hoan_thanh", ECDateUtils.format(ECDateUtils.TIMESTAMP, finishTime));
        String password = StringUtil.randomString(6);
        exts.put("ma_tra_cuu", password);
        String urlCode = StringUtil.randomString(10) + "-" + ECDateUtils.currentTimeMillis();
        exts.put("trang_tra_cuu", signUrl + urlCode);
        sendEmailComplete(ecDocument.get(), exts, urlCode);
        return CompletableFuture.completedFuture(1);
    }

    @Async
    public void sendSmsComplete(ECDocuments doc, Map<String, Object> exts, String urlCode) {
        log.info("BEGIN sending sms complete with document = {}", doc.getId());
        ECSendSms ecSendSms = sendSmsRepository.getECSendSmsByCompanyId(doc.getCompanyId());
        if (ecSendSms == null) {
            log.error("[sendSms] Not found email configuration of this company {}", doc.getCompanyId());
            return;
        }
        List<Integer> roles = Lists.newArrayList(ECAssigneeType.VIEWER.ordinal(), ECAssigneeType.SIGNATURE.ordinal());
        List<ECDocumentAssignee> assignees = assigneeRepository.getReminderAssigneeByAssignType(doc.getId(), roles);
        if (CollectionUtils.isEmpty(assignees)) {
            log.error("[sendSms] Not found any assignees with view role");
            return;
        }
        Optional<ECDocumentTypes> ecDocumentTypes = ecDocumentTypeRepository.findById(doc.getDocTypeId());
        if (!ecDocumentTypes.isPresent()) {
            log.error("[documentTypeId] Not found document type", doc.getDocTypeId());
            return;
        }
        List<EmailTemplateDto> templateDtos = notificationTemplate.getEmailTemplateByCompanyId(EmailTemplate.COMPLETE_DOCUMENT, doc.getCompanyId());
        List<ISendNotification> senders = Lists.newArrayList();
        // lưu thời gian hoàn thành


        for (ECDocumentAssignee item : assignees) {
            exts.put("trang_tra_cuu", signUrl + urlCode);
            exts.put("loai_hop_dong", ecDocumentTypes.get().getTypeName());
            exts.put("ma_hop_dong", doc.getCode());
            ISendNotification mailNotify = sendSms(doc, ecSendSms, exts, item, templateDtos);
            if (mailNotify != null) {
                senders.add(mailNotify);
                item.setUrlCode(urlCode);
                item.setPassword(BCrypt.hashpw(exts.get("ma_tra_cuu").toString(), BCrypt.gensalt(10)));
                assigneeRepository.save(item);
            }
        }
        log.info("begin executing sending sms complete");
        processSendingEmail(senders);
        log.info("end executing sending sms complete");
        log.info("END sending sms complete with document = {}", doc.getId());
    }

    @Async
    public void sendEmailComplete(ECDocuments doc, Map<String, Object> exts, String urlCode) {
        try {
            log.info("BEGIN sending email complete with document = {}", doc.getId());
            ECSendEmail ecSendEmail = sendEmailRepository.getEmailSettingByCompany(doc.getCompanyId());
            if (ecSendEmail == null) {
                log.error("[sendEmail] Not found email configuration of this company {}", doc.getCompanyId());
                return;
            }
            List<Integer> roles = Lists.newArrayList(ECAssigneeType.VIEWER.ordinal(), ECAssigneeType.SIGNATURE.ordinal());
            List<ECDocumentAssignee> assignees = assigneeRepository.getReminderAssigneeByAssignType(doc.getId(), roles);
            if (CollectionUtils.isEmpty(assignees)) {
                log.error("[sendEmail] Not found any assignees with view role");
                return;
            }

            List<EmailTemplateDto> templateDtos = notificationTemplate.getEmailTemplateByCompanyId(EmailTemplate.COMPLETE_DOCUMENT, doc.getCompanyId());
            List<ISendNotification> senders = Lists.newArrayList();

            for (ECDocumentAssignee item : assignees) {
                exts.put("trang_tra_cuu", signUrl + urlCode);
                ISendNotification mailNotify = sendEmail(doc, ecSendEmail, exts, item, templateDtos);
                if (mailNotify != null) {
                    senders.add(mailNotify);
                    item.setUrlCode(urlCode);
                    item.setPassword(BCrypt.hashpw(exts.get("ma_tra_cuu").toString(), BCrypt.gensalt(10)));
                    assigneeRepository.save(item);
                }
            }
            log.info("begin executing sending email ");
            processSendingEmail(senders);
            log.info("end executing sending email ");
            log.info("END sending email with document = {}", doc.getId());
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Error:", e);
        }
    }

    @Async
    public CompletableFuture<Integer> reSendNotify(NotificationReSendMsgRequest notify) {
        List<ISendNotification> senders = Lists.newArrayList();
        Optional<ECDocumentConversation> conversation = documentConversationRepository.findById(notify.getConversationId());
        if (!conversation.isPresent()) {
            throw new ECBusinessException("Not found the document conversation in the system", HttpStatus.NOT_FOUND);
        }
        ECDocumentConversation dcConversation = conversation.get();
        ISendNotification mailNotify = reSendEmail(dcConversation);
        if (mailNotify != null) senders.add(mailNotify);
        log.info("begin executing re sending email & sms");
        processReSendingEmail(senders);
        log.info("end executing re sending email & sms");
        return CompletableFuture.completedFuture(1);
    }

    public ISendNotification reSendEmail(ECDocumentConversation ecDocumentConversation) {
        // lấy thông tin assignee
        Optional<ECDocumentAssignee> ecDocumentAssignee = assigneeRepository.findById(ecDocumentConversation.getSendId());
        if (!ecDocumentAssignee.isPresent()) {
            throw new ECBusinessException("Not found the assignee in the system", HttpStatus.NOT_FOUND);
        }
        ECDocumentAssignee assignee = ecDocumentAssignee.get();

        // build content
        Map<ECDocumentAssignee, String> contents = Maps.newConcurrentMap();
        contents.put(assignee, ecDocumentConversation.getContent());

        // lấy thông tin description template
        Optional<ECConversationTemplate> ecConversationTemplateOptional = ecConversationTemplateRepository.findById(ecDocumentConversation.getTemplateId());
        if (!ecConversationTemplateOptional.isPresent()) {
            throw new ECBusinessException("Not found the template in the system", HttpStatus.NOT_FOUND);
        }
        ECConversationTemplate ecConversationTemplate = ecConversationTemplateOptional.get();

        // lấy thông tin cấu hình gửi email
        ECSendEmail ecSendEmail = sendEmailRepository.getEmailSettingByCompany(ecDocumentConversation.getCompanyId());


        return new EmailSendNotification.EmailSendNotificationBuilder()
                .withContents(contents)
                .withSubject(ecConversationTemplate.getDescription())
                .withEmailConfiguration(ecSendEmail)
                .withCompany(ecDocumentConversation.getCompanyId())
                .withDocument(ecDocumentConversation.getDocId())
                .withTemplate(ecConversationTemplate.getId())
                .withConversation(ecDocumentConversation)
                .withDocumentAssignee(assigneeRepository)
                .withDocumentConversationRepository(documentConversationRepository)
                .build();
    }

    public ISendNotification sendEmail(ECDocuments document,
                                       ECSendEmail ecSendEmail,
                                       Map<String, Object> password,
                                       ECDocumentAssignee assignee,
                                       List<EmailTemplateDto> templateDtos) {
        if (ecSendEmail == null) return null;
        Optional<EmailTemplateDto> emailTemplateDtos = templateDtos.stream().filter(item -> item.getType() == ECNotiType.EMAIL.getValue()).findFirst();
        if (!ObjectUtils.isEmpty(assignee) && emailTemplateDtos.isPresent()) {
            EmailTemplateDto tmp = emailTemplateDtos.get();
            log.info(tmp.getStatus());
            if (tmp.getStatus() != 1) {
                log.info("No send email");
                return null;
            }
//            if (document.getSource() == 1) {
//                Optional<ECDocumentPartners> ecDocumentPartner = partnerRepository.findById(assignee.getPartnerId());
//                if (!ecDocumentPartner.isPresent()) {
//                    throw new ECBusinessException("Not found document partners in the system", HttpStatus.NOT_FOUND);
//                }
//                ECDocumentPartners partners = ecDocumentPartner.get();
//                if (partners.getOrgType() == ECPartnerType.INDIVIDUAL.getValue()) {
//                    log.info("No send email");
//                    return null;
//                }
//            }
            String template = !Strings.isNullOrEmpty(tmp.getCompany_template()) ? tmp.getCompany_template() : tmp.getSystem_template();
            if (!Strings.isNullOrEmpty(template)) {
                Map<ECDocumentAssignee, String> contents = Maps.newConcurrentMap();
                contents.put(assignee, buildContent(document, assignee, template, password));
                return new EmailSendNotification.EmailSendNotificationBuilder()
                        .withContents(contents)
                        .withSubject(tmp.getDescription())
                        .withEmailConfiguration(ecSendEmail)
                        .withCompany(document.getCompanyId())
                        .withDocument(document.getId())
                        .withTemplate(tmp.getId())
                        .withDocumentAssignee(assigneeRepository)
                        .build();
            } else {
                log.warn("sendMsg: with template invalid");
            }
        }
        return null;
    }

    private ISendNotification sendSms(ECDocuments document,
                                      ECSendSms ecSendSms,
                                      Map<String, Object> ext,
                                      ECDocumentAssignee assignee,
                                      List<EmailTemplateDto> templateDtos) {
        if (ecSendSms == null) return null;
        Optional<EmailTemplateDto> emailTemplateDtos = templateDtos.stream().filter(item -> item.getType() == ECNotiType.SMS.getValue()).findFirst();
        if (ObjectUtils.isEmpty(assignee) || !emailTemplateDtos.isPresent()) {
            log.info("Assignees is empty or Sms template is not existed");
            return null;
        }
        EmailTemplateDto tmp = emailTemplateDtos.get();
        log.info(tmp.getStatus());
        if (tmp.getStatus() != 1) {
            log.info("No send sms");
            return null;
        }
        String template = !Strings.isNullOrEmpty(tmp.getCompany_template()) ? tmp.getCompany_template() : tmp.getSystem_template();
        if (Strings.isNullOrEmpty(template)) return null;
        Map<ECDocumentAssignee, Map<String, String>> contents = Maps.newConcurrentMap();
        contents.put(assignee,
                ImmutableMap.of("content", removeAccent(buildContent(document, assignee, template, ext)), "assign_id", String.valueOf(assignee.getId()))
        );
        return new SmsSendNotification.SmsSendNotificationBuilder()
                .withSmsContents(contents)
                .withSmsConfiguration(ecSendSms)
                .withCompany(document.getCompanyId())
                .withCPCode(cpCode)
                .withDocument(document.getId())
                .withTemplate(tmp.getId())
                .withECDocumentAssigneeRepository(assigneeRepository)
                .build();
    }

    private String removeAccent(String s) {
        String temp = Normalizer.normalize(s, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        temp = pattern.matcher(temp).replaceAll("");
        return temp.replaceAll("đ", "d");
    }

    private String buildContent(ECDocuments document, ECDocumentAssignee assignee, String template, Map<String, Object> exts) {
        Context context = new Context();
        context.setVariable("ten_tai_lieu", document.getName());
        context.setVariable("ten_nguoi_nhan", assignee.getFullName());
        if (exts != null && !exts.isEmpty()) context.setVariables(exts);
        return this.templateEngine.process(template, context);
    }

    private String buildContentAcc(String template, Map<String, Object> exts) {
        Context context = new Context();
        if (exts != null && !exts.isEmpty()) context.setVariables(exts);
        return this.templateEngine.process(template, context);
    }

    @Async
    public CompletableFuture<Integer> sendMsg(NotificationRequest notificationRequest) throws MessagingException {
        Optional<ECDocuments> dc = documentRepository.findById(notificationRequest.getDocumentId());
        if (!dc.isPresent()) {
            throw new ECBusinessException("Not found the document in the system", HttpStatus.NOT_FOUND);
        }
        ECDocuments doc = dc.get();
        if (doc.getDeleteFlag() == 1) throw new ECBusinessException("The document is not available on the system");
        if (doc.getStatus() == 0) throw new ECBusinessException("This document is not active on the system");
        List<EmailTemplateDto> templateDtos = notificationTemplate.getEmailTemplateByCompanyId(notificationRequest.getTemplateName(), doc.getCompanyId());
        if (CollectionUtils.isEmpty(templateDtos))
            throw new ECBusinessException("The template notification has not been found");
        List<ECDocumentAssignee> assignees = assigneeRepository.getReminderAssigneeByIds(notificationRequest.getDocumentId(), notificationRequest.getAssignIds());
        if (CollectionUtils.isEmpty(assignees))
            throw new ECBusinessException("Do not have any assignee with this document");
        //
        List<ISendNotification> senders = Lists.newArrayList();
        List<Integer> companyIds = new ArrayList<>(new HashSet<>(assignees.stream().map(item -> item.getCompanyId()).collect(Collectors.toList())));
        List<ECSendEmail> cmpEmailSettings = sendEmailRepository.getEmailSettingByCompanyIds(companyIds);
        List<ECSendSms> cmpSmsSettings = sendSmsRepository.getECSendSmsByCompanyIds(companyIds);
        //
        Map<Integer, List<ECSendEmail>> emailMappers = cmpEmailSettings.stream().collect(Collectors.groupingBy(ECSendEmail::getCompanyId));
        Map<Integer, List<ECSendSms>> smsMappers = cmpSmsSettings.stream().collect(Collectors.groupingBy(ECSendSms::getCompanyId));
        //
        for (ECDocumentAssignee item : assignees) {
            ECNotificationType pe = ECNotificationType.INDEX.get(item.getNotiType());
            switch (pe) {
                case ALL:
                    log.info("sendMsg ALL send all method: {}", item.getNotiType());
                    List<ECSendEmail> ecSendEmails = emailMappers.get(doc.getCompanyId());
                    if (!CollectionUtils.isEmpty(ecSendEmails)) {
                        ISendNotification mailNotify = sendEmail(doc, ecSendEmails.get(0), notificationRequest.getExts(), item, templateDtos);
                        if (mailNotify != null) senders.add(mailNotify);
                    }
                    List<ECSendSms> ecSendSms = smsMappers.get(doc.getCompanyId());
                    if (!CollectionUtils.isEmpty(ecSendSms)) {
                        ISendNotification smsNotify = sendSms(doc, ecSendSms.get(0), notificationRequest.getExts(), item, templateDtos);
                        if (smsNotify != null) senders.add(smsNotify);
                    }
                    break;
                case SMS:
                    log.info("sendMsg SMS send all method: {}", item.getNotiType());
                    ecSendSms = smsMappers.get(doc.getCompanyId());
                    if (!CollectionUtils.isEmpty(ecSendSms)) {
                        ISendNotification smsNotify = sendSms(doc, ecSendSms.get(0), notificationRequest.getExts(), item, templateDtos);
                        if (smsNotify != null) senders.add(smsNotify);
                    }
                    break;
                case EMAIL:
                    log.info("sendMsg EMAIL send all method: {}", item.getNotiType());
                    ecSendEmails = emailMappers.get(doc.getCompanyId());
                    if (!CollectionUtils.isEmpty(ecSendEmails)) {
                        ISendNotification mailNotify = sendEmail(doc, ecSendEmails.get(0), notificationRequest.getExts(), item, templateDtos);
                        if (mailNotify != null) senders.add(mailNotify);
                    }
                    break;
                case NO_NOTIFY:
                    continue;
                default:
                    throw new ECBusinessException("Unsupported this notification type");
            }
        }
        log.info("begin executing sending email & sms");
        processSendingEmail(senders);
        log.info("end executing sending email & sms");

        return CompletableFuture.completedFuture(1);
    }

    private void processSendingEmail(List<ISendNotification> senders) {
        List<Single<ISendNotification>> process = senders.stream().map(sender -> Single.fromCallable(() -> {
            sender.setConversationRepository(documentConversationRepository);
            sender.sendMsg();
            return sender;
        })).collect(Collectors.toList());

        Single.zip(process, (r) -> 1).subscribeOn(Schedulers.io()).blockingGet();
    }

    private void processReSendingEmail(List<ISendNotification> senders) {
        List<Single<ISendNotification>> process = senders.stream().map(sender -> Single.fromCallable(() -> {
            sender.setConversationRepository(documentConversationRepository);
            sender.reSendMsg();
            return sender;
        })).collect(Collectors.toList());

        Single.zip(process, (r) -> 1).subscribeOn(Schedulers.io()).blockingGet();
    }

    @Async
    public CompletableFuture<Integer> reminder(ECDocuments doc, String template, Map<String, Object> exts, Integer id, String signUrl, String password) {
        if (doc.getDeleteFlag() == 1) throw new ECBusinessException("The document is not available on the system");
        if (doc.getStatus() == 0) throw new ECBusinessException("This document is not active on the system");
        List<EmailTemplateDto> templateDtos = notificationTemplate.getEmailTemplateByCompanyId(template, doc.getCompanyId());
        if (CollectionUtils.isEmpty(templateDtos))
            throw new ECBusinessException("The template notification has not been found");
        List<Integer> roles = Lists.newArrayList(ECAssigneeType.CREATOR.ordinal());
        List<ECDocumentAssignee> assignees = assigneeRepository.getReminderAssigneeByDocumentIdAndId(doc.getId(), roles, id);
        if (CollectionUtils.isEmpty(assignees))
            throw new ECBusinessException("Do not have any assignee with this document");
        List<ISendNotification> senders = Lists.newArrayList();

        List<Integer> companyIds = new ArrayList<>(new HashSet<>(assignees.stream().map(item -> item.getCompanyId()).collect(Collectors.toList())));
        List<ECSendEmail> cmpEmailSettings = sendEmailRepository.getEmailSettingByCompanyIds(companyIds);
        List<ECSendSms> cmpSmsSettings = sendSmsRepository.getECSendSmsByCompanyIds(companyIds);

        Map<Integer, List<ECSendEmail>> emailMappers = cmpEmailSettings.stream().collect(Collectors.groupingBy(ECSendEmail::getCompanyId));
        Map<Integer, List<ECSendSms>> smsMappers = cmpSmsSettings.stream().collect(Collectors.groupingBy(ECSendSms::getCompanyId));

        for (ECDocumentAssignee item : assignees) {
            ECNotificationType pe = ECNotificationType.INDEX.get(item.getNotiType());
            switch (pe) {
                case ALL:
                    log.info("sendMsg ALL send all method: {}", item.getNotiType());
                    List<ECSendEmail> ecSendEmails = emailMappers.get(doc.getCompanyId());
                    if (!CollectionUtils.isEmpty(ecSendEmails)) {
                        ISendNotification mailNotify = sendEmail(doc, ecSendEmails.get(0), exts, item, templateDtos);
                        if (mailNotify != null) senders.add(mailNotify);
                    }
                    List<ECSendSms> ecSendSms = smsMappers.get(doc.getCompanyId());
                    if (!CollectionUtils.isEmpty(ecSendSms)) {
                        ISendNotification smsNotify = sendSms(doc, ecSendSms.get(0), exts, item, templateDtos);
                        if (smsNotify != null) senders.add(smsNotify);
                    }
                    break;
                case SMS:
                    log.info("sendMsg SMS send all method: {}", item.getNotiType());
                    ecSendSms = smsMappers.get(doc.getCompanyId());
                    if (!CollectionUtils.isEmpty(ecSendSms)) {
                        ISendNotification smsNotify = sendSms(doc, ecSendSms.get(0), exts, item, templateDtos);
                        if (smsNotify != null) senders.add(smsNotify);
                    }
                    break;
                case EMAIL:
                    log.info("sendMsg EMAIL send all method: {}", item.getNotiType());
                    ecSendEmails = emailMappers.get(doc.getCompanyId());
                    if (!CollectionUtils.isEmpty(ecSendEmails)) {
                        ISendNotification mailNotify = sendEmail(doc, ecSendEmails.get(0), exts, item, templateDtos);
                        if (mailNotify != null) senders.add(mailNotify);
                    }
                    break;
                case NO_NOTIFY:
                    continue;
                default:
                    throw new ECBusinessException("Unsupported this notification type");
            }
            if (!signUrl.equals("") && !password.equals("")) {
                item.setUrlCode(signUrl);
                item.setPassword(BCrypt.hashpw(password, BCrypt.gensalt(10)));
            }
        }

        log.info("begin executing sending email & sms");
        processSendingEmail(senders);
        log.info("end executing sending email & sms");
        return CompletableFuture.completedFuture(1);
    }


    private boolean checkRemimdedOrNot(int docId, List<EmailTemplateDto> templateDtos, String type) {
        Optional<EmailTemplateDto> emailTemplateDto = null;
        if (type.equals("SMS")) {
            emailTemplateDto = templateDtos.stream().filter(item -> item.getType() == ECNotiType.SMS.getValue()).findFirst();
        } else {
            emailTemplateDto = templateDtos.stream().filter(item -> item.getType() == ECNotiType.EMAIL.getValue()).findFirst();
        }

        EmailTemplateDto tmp = emailTemplateDto.get();
        List<ECDocumentConversation> ECDocumentConversations = documentConversationRepository.getRemindedECDocumentByDocIdAndTemplateId(docId, tmp.getId());
        if (!CollectionUtils.isEmpty(ECDocumentConversations)) {
            return false;
        }

        return true;
    }

    @Async
    public CompletableFuture<Integer> sendMsgAcc(NotificationAccRequest notificationRequest) throws MessagingException {

        List<EmailTemplateDto> templateDtos = notificationTemplate.getEmailTemplateByCompanyId(notificationRequest.getTemplateName());
        if (CollectionUtils.isEmpty(templateDtos))
            throw new ECBusinessException("The template notification has not been found");

        ECSendSms ecSendSms;
        ECSendEmail ecSendEmail;
        ISendNotification mailNotify;
        ISendNotification smsNotify;
        List<ISendNotification> senders = Lists.newArrayList();
        ECNotificationType pe = ECNotificationType.INDEX.get(notificationRequest.getType());
        switch (pe) {
            case ALL:
                log.info("sendMsg ALL send all method: {}", notificationRequest.getType());
                ecSendEmail = sendEmailRepository.getEmailSettingByCompany(-1);
                mailNotify = sendEmailAcc(ecSendEmail, notificationRequest.getExts(), templateDtos, notificationRequest.getEmail());
                if (mailNotify != null) senders.add(mailNotify);
                ecSendSms = sendSmsRepository.getECSendSmsByCompanyId(-1);
                smsNotify = sendSmsAcc(ecSendSms, notificationRequest.getExts(), templateDtos, notificationRequest.getNumberPhone());
                if (smsNotify != null) senders.add(smsNotify);
                break;
            case SMS:
                log.info("sendMsg SMS send all method: {}", notificationRequest.getType());
                ecSendSms = sendSmsRepository.getECSendSmsByCompanyId(-1);
                smsNotify = sendSmsAcc(ecSendSms, notificationRequest.getExts(), templateDtos, notificationRequest.getNumberPhone());
                if (smsNotify != null) senders.add(smsNotify);
                break;
            case EMAIL:
                log.info("sendMsg EMAIL send all method: {}", notificationRequest.getType());
                ecSendEmail = sendEmailRepository.getEmailSettingByCompany(-1);
                mailNotify = sendEmailAcc(ecSendEmail, notificationRequest.getExts(), templateDtos, notificationRequest.getEmail());
                if (mailNotify != null) senders.add(mailNotify);
                break;
            default:
                throw new ECBusinessException("Unsupported this notification type");
        }
        log.info("begin executing sending email & sms");
        processSendingEmail(senders);
        log.info("end executing sending email & sms");
        return CompletableFuture.completedFuture(1);
    }

    private ISendNotification sendEmailAcc(ECSendEmail ecSendEmail,
                                           Map<String, Object> password,
                                           List<EmailTemplateDto> templateDtos,
                                           String cc) {
        if (ecSendEmail == null) return null;
        Optional<EmailTemplateDto> emailTemplateDtos = templateDtos.stream().filter(item -> item.getType() == ECNotiType.EMAIL.getValue()).findFirst();
        if (emailTemplateDtos.isPresent()) {
            EmailTemplateDto tmp = emailTemplateDtos.get();
            String template = !Strings.isNullOrEmpty(tmp.getCompany_template()) ? tmp.getCompany_template() : tmp.getSystem_template();
            if (!Strings.isNullOrEmpty(template)) {
                Map<String, String> contents = Maps.newConcurrentMap();
                contents.put(cc, buildContentAcc(template, password));
                return new EmailSendNotification.EmailSendNotificationBuilder()
                        .withContentsAcc(contents)
                        .withSubject(tmp.getDescription())
                        .withEmailConfiguration(ecSendEmail)
                        .withTemplate(tmp.getId())
                        .isCheckAcc(true)
                        .build();
            } else {
                log.warn("sendMsg: with template invalid");
            }
        }
        return null;
    }

    private ISendNotification sendSmsAcc(ECSendSms ecSendSms,
                                         Map<String, Object> password,
                                         List<EmailTemplateDto> templateDtos,
                                         String cc) {

        Optional<EmailTemplateDto> emailTemplateDtos = templateDtos.stream().filter(item -> item.getType() == ECNotiType.SMS.getValue()).findFirst();
        if (!emailTemplateDtos.isPresent()) {
            log.info("Sms template is not existed");
            return null;
        }
        EmailTemplateDto tmp = emailTemplateDtos.get();
        if (Strings.isNullOrEmpty(tmp.getSystem_template())) return null;
        Map<ECDocumentAssignee, Map<String, String>> contents = Maps.newConcurrentMap();
        ECDocumentAssignee assignee = new ECDocumentAssignee();
        assignee.setPhone(cc);
        contents.put(assignee,
                ImmutableMap.of("content", buildContentAcc(tmp.getSystem_template(), password), "assign_id", "-1"));
        return new SmsSendNotification.SmsSendNotificationBuilder()
                .withSmsContents(contents)
                .withSmsConfiguration(ecSendSms)
                .withTemplate(tmp.getId())
                .withECDocumentAssigneeRepository(assigneeRepository)
                .build();
    }
}
