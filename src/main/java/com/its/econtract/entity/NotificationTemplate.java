package com.its.econtract.entity;

import com.its.econtract.dto.EmailTemplateDto;

import java.util.Date;
import java.util.List;

public interface NotificationTemplate {
    List<EmailTemplateDto> getEmailTemplateByCompanyId(String template, int companyId);

    List<EmailTemplateDto> getEmailTemplateByCompanyId(String template);

    List<ECDocuments> getDocumentByDateAndStatus(int id , List<Integer> listOfStatus, Date begin, Date end, int pageSize);

    List<ECDocuments> getDocumentByDateAndStatusAndNotReminderType(int id , List<Integer> listOfStatus, int reimderType, Date begin, Date end, int pageSize);

    List<ECDocuments> getDocumentByDateExpiredAndStatus(int id , List<Integer> listOfStatus, Date begin, Date end, int pageSize);
}
