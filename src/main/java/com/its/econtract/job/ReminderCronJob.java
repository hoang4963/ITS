package com.its.econtract.job;

import com.its.econtract.services.ECDocumentService;
import lombok.extern.log4j.Log4j2;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

@Log4j2
@DisallowConcurrentExecution
@Component
public class ReminderCronJob extends QuartzJobBean {

    @Autowired
    private ECDocumentService service;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        log.info("ReminderCronJob Start................"+context.getJobDetail().getKey().getName());
        switch (context.getJobDetail().getKey().getName()) {

            case "REMINDER_NEAR_EXPRIE_DOC": //sắp hết hạn giao kết
                service.remindNearExpireDocument();
                break;
            case "REMINDER_EXPRIED_DOC": // hết hạn giao kết
                service.remindExpiredDocument();
                break;
            case "HANDLE_EXPRIED_DOC": // hết hạn giao kết (update status)
                service.handleExpiredDocument();
                break;
            case "REMINDER_NEAR_EXPRIE_DOC_DATE": //sắp hết hạn hiệu lực
                service.remindNearExpireDocumentDate();
                break;
            case "REMINDER_EXPRIED_DOC_DATE": // hết hạn hiệu lực
                service.remindExpiredDocumentDate();
                break;
            case "HANDLE_EXPRIED_DOC_DATE": // hết hạn hiệu lực (update status)
                service.handleDateExpiredDocument();
                break;

        }
        log.info("ReminderCronJob End................" + context.getJobDetail().getKey().getName());
    }
}
