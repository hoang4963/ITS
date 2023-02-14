package com.its.econtract.services;

import com.google.common.collect.Maps;
import com.its.econtract.entity.ECDocuments;
import com.its.econtract.entity.NotificationTemplate;
import com.its.econtract.entity.enums.ECRemimderType;
import com.its.econtract.facade.ECNotificationFacade;
import com.its.econtract.utils.ECDateUtils;
import com.its.econtract.entity.enums.ECDocumentType;
import com.its.econtract.repository.ECDocumentRepository;
import com.its.econtract.utils.StringUtil;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Log4j2
@Component
public class ECDocumentService {

    @Value("${remind.date.1}")
    private int reminder = 5;

    private int pageSize = 50;

    @Autowired
    private NotificationTemplate notificationTemplate;

    @Autowired
    private ECNotificationFacade facade;

    @Autowired
    private ECDocumentRepository ecDocumentRepository;

    @Value("${sign.url}")
    private String signUrl;

    /**
     * Tiến trình gửi mail sắp hết hạn giao kết hợp đồng
     */
    public void remindNearExpireDocument() {
        int id = -1;
        log.info("BEGIN ");
        String template = "NEARLY_OVERDUE_DOCUMENT";
        Date today = new Date();
        Date checkDate = ECDateUtils.addDays(today, reminder);
        Date begin = ECDateUtils.setStartTimeOfDay(checkDate);
        Date end = ECDateUtils.setEndTimeOfDay(checkDate);
        List<Integer> listOfStatus = Arrays.asList(ECDocumentType.ChoDuyet.getValue(), ECDocumentType.ChoKySo.getValue());
        while (true) {
            List<ECDocuments> documents = notificationTemplate.getDocumentByDateAndStatusAndNotReminderType(id, listOfStatus, ECRemimderType.NEAR_EXPIRE_LEASING.getValue(), begin, end, pageSize);
            for (ECDocuments document : documents) {
                Map<String, Object> contents = Maps.newConcurrentMap();
                contents.put("ten_tai_lieu", document.getName());
                contents.put("han_tai_lieu", document.getExpiredDate());
                String password = StringUtil.randomString(6);
                contents.put("ma_tra_cuu", password);
                String urlCode = StringUtil.randomString(10) + "-" + ECDateUtils.currentTimeMillis();
                contents.put("trang_tra_cuu", signUrl + urlCode);
                try {
                    facade.reminder(document, template, contents, document.getCurrentAssigneeId(), urlCode, password);
                } catch (Exception e) {
                    log.error("remindNearExpireDocument:", e);
                }
                document.setRemimderType(ECRemimderType.NEAR_EXPIRE_LEASING.getValue());
                ecDocumentRepository.save(document);
            }
            if (documents.size() < pageSize) break;
            id = documents.get(documents.size() - 1).getId();
        }
    }

    /**
     * Tiến trình gửi mail hết hạn giao kết hợp đồng
     */
    public void remindExpiredDocument() {
        int id = -1;
        log.info("BEGIN ");
        String template = "OVERDUE_DOCUMENT";
        Date today = new Date();
        Date checkDate = ECDateUtils.addDays(today, -1);
        Date begin = ECDateUtils.setStartTimeOfDay(checkDate);
        Date end = ECDateUtils.setEndTimeOfDay(checkDate);
        List<Integer> listOfStatus = Arrays.asList(ECDocumentType.QuaHan.getValue());
        while (true) {
            List<ECDocuments> documents = notificationTemplate.getDocumentByDateAndStatusAndNotReminderType(id, listOfStatus, ECRemimderType.EXPIRED_LEASING.getValue(), begin, end, pageSize);
            if (CollectionUtils.isEmpty(documents)) break;
            for (ECDocuments document : documents) {
                Map<String, Object> contents = Maps.newConcurrentMap();
                contents.put("ten_tai_lieu", document.getName());
                contents.put("han_tai_lieu", document.getExpiredDate());
                try {
                    facade.reminder(document, template, contents, document.getCurrentAssigneeId(), "", "");
                } catch (Exception e) {
                    log.error("remindExpiredDocument:", e);
                }
                document.setRemimderType(ECRemimderType.EXPIRED_LEASING.getValue());
                ecDocumentRepository.save(document);
            }
            id = documents.get(documents.size() - 1).getId();
            if (documents.size() < pageSize) break;
        }

    }

    /**
     * Tiến trình gửi mail sắp hết hạn hợp đồng
     */
    public void remindNearExpireDocumentDate() {
        int id = -1;
        log.info("BEGIN ");
        String template = "NEARLY_OVERDUE_DOCUMENT_DATE";
        Date today = new Date();
        Date checkDate = ECDateUtils.addDays(today, reminder);
        Date begin = ECDateUtils.setStartTimeOfDay(checkDate);
        Date end = ECDateUtils.setEndTimeOfDay(checkDate);
        List<Integer> listOfStatus = Collections.singletonList(ECDocumentType.HoanThanh.getValue());
        while (true) {
            List<ECDocuments> documents = notificationTemplate.getDocumentByDateAndStatusAndNotReminderType(id, listOfStatus, ECRemimderType.NEAR_EXPIRE.getValue(), begin, end, pageSize);
            for (ECDocuments document : documents) {
                Map<String, Object> contents = Maps.newConcurrentMap();
                contents.put("ten_tai_lieu", document.getName());
                contents.put("han_tai_lieu", document.getExpiredDate());
                String password = StringUtil.randomString(6);
                contents.put("ma_tra_cuu", password);
                String urlCode = StringUtil.randomString(10) + "-" + ECDateUtils.currentTimeMillis();
                contents.put("trang_tra_cuu", signUrl + urlCode);
                try {
                    facade.reminder(document, template, contents, document.getCurrentAssigneeId(), urlCode, password);
                } catch (Exception e) {
                    log.error("remindNearExpireDocument:", e);
                }
                document.setRemimderType(ECRemimderType.NEAR_EXPIRE.getValue());
                ecDocumentRepository.save(document);
            }
            if (documents.size() < pageSize) break;
            id = documents.get(documents.size() - 1).getId();
        }
    }

    /**
     * Tiến trình gửi mail hết hạn hợp đồng
     */
    public void remindExpiredDocumentDate() {
        int id = -1;
        log.info("BEGIN ");
        String template = "OVERDUE_DOCUMENT_DATE";
        Date today = new Date();
        Date checkDate = ECDateUtils.addDays(today, -1);
        Date begin = ECDateUtils.setStartTimeOfDay(checkDate);
        Date end = ECDateUtils.setEndTimeOfDay(checkDate);
        List<Integer> listOfStatus = Collections.singletonList(ECDocumentType.HetHan.getValue());
        while (true) {
            List<ECDocuments> documents = notificationTemplate.getDocumentByDateAndStatusAndNotReminderType(id, listOfStatus, ECRemimderType.EXPIRE.getValue(), begin, end, pageSize);
            if (CollectionUtils.isEmpty(documents)) break;
            for (ECDocuments document : documents) {
                Map<String, Object> contents = Maps.newConcurrentMap();
                contents.put("ten_tai_lieu", document.getName());
                contents.put("han_tai_lieu", document.getExpiredDate());
                try {
                    facade.reminder(document, template, contents, document.getCurrentAssigneeId(), "", "");
                } catch (Exception e) {
                    log.error("remindExpiredDocument:", e);
                }
                document.setRemimderType(ECRemimderType.EXPIRE.getValue());
                ecDocumentRepository.save(document);
            }
            id = documents.get(documents.size() - 1).getId();
            if (documents.size() < pageSize) break;
        }

    }

    public void handleExpiredDocument() {
        int id = -1;
        log.info("BEGIN ");
        Date today = new Date();
        Date checkDate = ECDateUtils.addDays(today, -1);
        Date begin = ECDateUtils.setStartTimeOfDay(checkDate);
        Date end = ECDateUtils.setEndTimeOfDay(checkDate);
        List<Integer> listOfStatus = Arrays.asList(ECDocumentType.ChoDuyet.getValue(), ECDocumentType.ChoKySo.getValue());
        while (true) {
            List<ECDocuments> documents = notificationTemplate.getDocumentByDateAndStatus(id, listOfStatus, begin, end, pageSize);
            if (CollectionUtils.isEmpty(documents)) break;
            List<Integer> listOfIds = documents.stream().map(item -> item.getId()).collect(Collectors.toList());
            try {
                updateStateDocumentById(listOfIds);
            } catch (Exception e) {
                log.error("updateStateDocumentById error & rollback = {}", listOfIds);
            }
            id = documents.get(documents.size() - 1).getId();
            if (documents.size() < pageSize) break;
        }
    }

    public void handleDateExpiredDocument() {
        int id = -1;
        log.info("BEGIN ");
        Date today = new Date();
        Date checkDate = ECDateUtils.addDays(today, -1);
        Date begin = ECDateUtils.setStartTimeOfDay(checkDate);
        Date end = ECDateUtils.setEndTimeOfDay(checkDate);
        List<Integer> listOfStatus = Arrays.asList(ECDocumentType.HoanThanh.getValue());
        while (true) {
            List<ECDocuments> documents = notificationTemplate.getDocumentByDateExpiredAndStatus(id, listOfStatus, begin, end, pageSize);
            if (CollectionUtils.isEmpty(documents)) break;
            List<Integer> listOfIds = documents.stream().map(item -> item.getId()).collect(Collectors.toList());
            try {
                updateStateDocumentByIdDateExpired(listOfIds);
            } catch (Exception e) {
                log.error("updateStateDocumentById error & rollback = {}", listOfIds);
            }
            id = documents.get(documents.size() - 1).getId();
            if (documents.size() < pageSize) break;
        }
    }

    protected void updateStateDocumentById(List<Integer> listOfIds) {
        ecDocumentRepository.updateStateDocumentByIds(ECDocumentType.QuaHan.getValue(), new Date(), listOfIds);
    }

    protected void updateStateDocumentByIdDateExpired(List<Integer> listOfIds) {
        ecDocumentRepository.updateStateDocumentByIds(ECDocumentType.HetHan.getValue(), new Date(), listOfIds);
    }

}
