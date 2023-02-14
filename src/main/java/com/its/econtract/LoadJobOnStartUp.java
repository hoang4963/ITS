package com.its.econtract;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.its.econtract.entity.schedule.ECScheduleJobInfo;
import com.its.econtract.repository.ECSchedulerJobInfoRepository;
import com.its.econtract.services.ECSchedulerService;

@Component
@PropertySource({"classpath:quartz.properties"})
public class LoadJobOnStartUp {

	@Autowired
    private ECSchedulerJobInfoRepository ecSchedulerJobInfoRepository;

	@Autowired
    private ECSchedulerService schedulerService;

	@Value("${cron.expression.reminder.near.expire.doc}")
    private String cronExpressionReminderNearExpireDoc;

	@Value("${cron.expression.reminder.expired.doc}")
    private String cronExpressionReminderExpiredDoc;

	@Value("${cron.expression.handle.expired.doc}")
    private String cronExpressionHandleExpiredDoc;

    // Gửi email
    // sắp hết hạn giao kết
	private final String REMINDER_NEAR_EXPRIE_DOC_JOB = "REMINDER_NEAR_EXPRIE_DOC";
    // sắp hết hạn hiệu lực
	private final String REMINDER_NEAR_EXPRIE_DOC_DATE_JOB = "REMINDER_NEAR_EXPRIE_DOC_DATE";
    // hết hạn giao kết
	private final String REMINDER_EXPRIED_DOC_JOB = "REMINDER_EXPRIED_DOC";
    // hết hạn hiệu lực
	private final String REMINDER_EXPRIED_DOC_DATE_JOB = "REMINDER_EXPRIED_DOC_DATE";
    // Update status
    // hết hạn giao kết (update status)
	private final String HANDLE_EXPRIED_DOC_JOB = "HANDLE_EXPRIED_DOC";
    // hết hạn hiệu lực (update status)
	private final String HANDLE_EXPRIED_DOC_DATE_JOB = "HANDLE_EXPRIED_DOC_DATE";

	//create or update main job
	@EventListener(ApplicationReadyEvent.class)
    public void loadData()
    {
		/*
        /// Gửi email
        // sắp hết hạn giao kết
		setupMainJob(REMINDER_NEAR_EXPRIE_DOC_JOB, "REMINDER NEAR EXPRIE DOC", cronExpressionReminderNearExpireDoc);

        // sắp hết hạn hiệu lực
		setupMainJob(REMINDER_NEAR_EXPRIE_DOC_DATE_JOB, "REMINDER NEAR EXPRIE DOC DATE", cronExpressionReminderNearExpireDoc);

        // hết hạn giao kết
		setupMainJob(REMINDER_EXPRIED_DOC_JOB, "REMINDER EXPRIED DOC", cronExpressionReminderExpiredDoc);

        // hết hạn hiệu lực
		setupMainJob(REMINDER_EXPRIED_DOC_DATE_JOB, "REMINDER EXPRIED DOC DATE", cronExpressionReminderExpiredDoc);

        /// Update status
        // hết hạn giao kết (update status)
		setupMainJob(HANDLE_EXPRIED_DOC_JOB, "HANDLE EXPRIED DOC", cronExpressionHandleExpiredDoc);

        // hết hạn hiệu lực (update status)
		setupMainJob(HANDLE_EXPRIED_DOC_DATE_JOB, "HANDLE EXPRIED DOC DATE JOB", cronExpressionHandleExpiredDoc);
		 */
    }

	private void setupMainJob(String name, String description, String cron) {
		ECScheduleJobInfo infoJob = ecSchedulerJobInfoRepository.findByJobName(name);
		if (infoJob == null) {
			infoJob = new ECScheduleJobInfo();
			infoJob.setId(0);
			infoJob.setJobName(name);
			infoJob.setDesc(description);
		}

		infoJob.setCronExpression(cron);

        ECScheduleJobInfo result = schedulerService.saveOrUpdate(infoJob);
        schedulerService.startJobNow(result.getId());
	}

}
