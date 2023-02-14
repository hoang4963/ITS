package com.its.econtract.entity.impl;

import com.its.econtract.dto.EmailTemplateDto;
import com.its.econtract.entity.ECDocuments;
import com.its.econtract.entity.NotificationTemplate;

import lombok.extern.log4j.Log4j2;

import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Log4j2
@Service
public class NotificationTemplateImpl implements NotificationTemplate {

    @PersistenceContext
    private EntityManager entityManager;

    public List<EmailTemplateDto> getEmailTemplateByCompanyId(String template, int companyId) {
        Query query = entityManager.createNativeQuery(
                "SELECT ct.id, ct.type, ct.template AS system_template, cct.template AS company_template, ct.template_description,cct.status FROM ec_s_conversation_templates ct LEFT JOIN ec_s_company_conversation_templates cct ON ct.id = cct.template_id AND cct.company_id = ?1 WHERE ct.template_name = ?2");
        query.setParameter(1, companyId);
        query.setParameter(2, template);
        List<Object[]> results = query.getResultList();
        List<EmailTemplateDto> emails = results.stream()
                .map(item -> new EmailTemplateDto(((BigInteger) item[0]).intValue(), ((Byte) item[1]).intValue(), String.valueOf(item[2]), item[3] == null ? "" : String.valueOf(item[3]), String.valueOf(item[4]), item[5] == null ? 1 : ((Byte) item[5]).intValue()))
                .collect(Collectors.toList());
        return emails;
    }

    @Override
    public List<EmailTemplateDto> getEmailTemplateByCompanyId(String template) {
        Query query = entityManager.createNativeQuery(
                "SELECT ct.id, ct.type, ct.template AS system_template, ct.template_description FROM ec_s_conversation_templates ct WHERE ct.template_name = ?1");
        query.setParameter(1, template);
        List<Object[]> results = query.getResultList();
        List<EmailTemplateDto> emails = results.stream()
                .map(item -> new EmailTemplateDto(((BigInteger) item[0]).intValue(), ((Byte) item[1]).intValue(), String.valueOf(item[2]), "", String.valueOf(item[3]), 1))
                .collect(Collectors.toList());
        return emails;
    }

    @Override
    public List<ECDocuments> getDocumentByDateAndStatus(int id, List<Integer> listOfStatus, Date begin, Date end, int pageSize) {
        Query query = entityManager.createNativeQuery(
                "SELECT d.* FROM ec_documents d WHERE d.id > ?1 and d.document_state in ?2 and d.expired_date between ?3 and ?4 and d.status = true and d.delete_flag = 0 order by id asc limit ?5 ", ECDocuments.class);
        query.setParameter(1, id);
        query.setParameter(2, listOfStatus);
        query.setParameter(3, begin);
        query.setParameter(4, end);
        query.setParameter(5, pageSize);
        return query.getResultList();
    }

    @Override
    public List<ECDocuments> getDocumentByDateExpiredAndStatus(int id, List<Integer> listOfStatus, Date begin, Date end, int pageSize) {
        Query query = entityManager.createNativeQuery(
                "SELECT d.* FROM ec_documents d WHERE d.id > ?1 and d.document_state in ?2 and d.doc_expired_date between ?3 and ?4 and d.status = true and d.delete_flag = 0 order by id asc limit ?5 ", ECDocuments.class);
        query.setParameter(1, id);
        query.setParameter(2, listOfStatus);
        query.setParameter(3, begin);
        query.setParameter(4, end);
        query.setParameter(5, pageSize);
        return query.getResultList();
    }

    @Override
    public List<ECDocuments> getDocumentByDateAndStatusAndNotReminderType(int id, List<Integer> listOfStatus, int reimderType, Date begin, Date end, int pageSize) {
        Query query = entityManager.createNativeQuery(
                "SELECT d.* FROM ec_documents d WHERE d.id > ?1 and d.document_state in ?2 and remimder_type != ?3 and d.expired_date between ?4 and ?5 and d.status = true and d.delete_flag = 0 order by id asc limit ?6 ", ECDocuments.class);
        query.setParameter(1, id);
        query.setParameter(2, listOfStatus);
        query.setParameter(3, reimderType);
        query.setParameter(4, begin);
        query.setParameter(5, end);
        query.setParameter(6, pageSize);
        return query.getResultList();
    }

}
