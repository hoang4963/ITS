package com.its.econtract.job;

import lombok.extern.log4j.Log4j2;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

@Log4j2
@DisallowConcurrentExecution
@Component
public class SimpleCronJob extends QuartzJobBean {

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        log.info("SimpleCronJob Start................");
        System.out.println("[SimpleCronJob] |=================>>");
        log.info("SimpleCronJob End................");
    }
}
