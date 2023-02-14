package com.its.econtract.services;

import com.google.common.base.Strings;
import com.its.econtract.entity.schedule.ECScheduleJobInfo;
import com.its.econtract.exceptions.ECBusinessException;
import com.its.econtract.job.JobScheduleCreator;
import com.its.econtract.job.ReminderCronJob;
import com.its.econtract.job.SimpleCronJob;
import com.its.econtract.repository.ECSchedulerJobInfoRepository;
import lombok.extern.log4j.Log4j2;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Log4j2
@Service
public class ECSchedulerService {

    @Autowired
    @Qualifier("schedulerFactoryBean")
    private Scheduler scheduler;

    @Autowired
    private SchedulerFactoryBean schedulerFactoryBean;

    @Autowired
    private ECSchedulerJobInfoRepository ecSchedulerJobInfoRepository;

    @Autowired
    private ApplicationContext context;

    @Autowired
    private JobScheduleCreator scheduleCreator;

    public List<ECScheduleJobInfo> getAllJobList() {
        return ecSchedulerJobInfoRepository.findAll();
    }

    public boolean deleteJob(int id) {
        Optional<ECScheduleJobInfo> jobInfo = ecSchedulerJobInfoRepository.findById(id);
        if (!jobInfo.isPresent()) throw new ECBusinessException("Not found this job in the system");
        ECScheduleJobInfo getJobInfo = jobInfo.get();
        try {
            ecSchedulerJobInfoRepository.delete(getJobInfo);
            log.info(">>>>> jobName = [" + getJobInfo.getJobName() + "]" + " deleted.");
            return schedulerFactoryBean.getScheduler().deleteJob(new JobKey(getJobInfo.getJobName(), getJobInfo.getJobGroup()));
        } catch (SchedulerException e) {
            log.error("Failed to delete job - {}", getJobInfo.getJobName(), e);
            return false;
        }
    }

    public boolean pauseJob(int id) {
        Optional<ECScheduleJobInfo> jobInfo = ecSchedulerJobInfoRepository.findById(id);
        if (!jobInfo.isPresent()) throw new ECBusinessException("Not found this job in the system");
        try {
            ECScheduleJobInfo getJobInfo = jobInfo.get();
            getJobInfo.setJobStatus("PAUSED");
            ecSchedulerJobInfoRepository.save(getJobInfo);
            schedulerFactoryBean.getScheduler().pauseJob(new JobKey(getJobInfo.getJobName(), getJobInfo.getJobGroup()));
            log.info(">>>>> jobName = [" + getJobInfo.getJobName() + "]" + " paused.");
            return true;
        } catch (SchedulerException e) {
            log.error("Failed to pause job - {}", jobInfo.get().getJobName(), e);
            return false;
        }
    }

    public boolean resumeJob(int id) {
        Optional<ECScheduleJobInfo> jobInfo = ecSchedulerJobInfoRepository.findById(id);
        if (!jobInfo.isPresent()) throw new ECBusinessException("Not found this job in the system");
        ECScheduleJobInfo getJobInfo = jobInfo.get();
        try {
            getJobInfo.setJobStatus("RESUMED");
            ecSchedulerJobInfoRepository.save(getJobInfo);
            schedulerFactoryBean.getScheduler().resumeJob(new JobKey(getJobInfo.getJobName(), getJobInfo.getJobGroup()));
            log.info(">>>>> jobName = [" + getJobInfo.getJobName() + "]" + " resumed.");
            return true;
        } catch (SchedulerException e) {
            log.error("Failed to resume job - {}", getJobInfo.getJobName(), e);
            return false;
        }
    }

    public boolean startJobNow(int id) {
        Optional<ECScheduleJobInfo> jobInfo = ecSchedulerJobInfoRepository.findById(id);
        if (!jobInfo.isPresent()) throw new ECBusinessException("Not found this job in the system");
        try {
            ECScheduleJobInfo getJobInfo = jobInfo.get();
            getJobInfo.setJobStatus("SCHEDULED & STARTED");
            ecSchedulerJobInfoRepository.save(getJobInfo);
            schedulerFactoryBean.getScheduler().triggerJob(new JobKey(getJobInfo.getJobName(), getJobInfo.getJobGroup()));
            log.info(">>>>> jobName = [" + getJobInfo.getJobName() + "]" + " scheduled and started now.");
            return true;
        } catch (SchedulerException e) {
            log.error("Failed to start new job - {}", id, e);
            return false;
        }
    }

    @SuppressWarnings("deprecation")
    public ECScheduleJobInfo saveOrUpdate(ECScheduleJobInfo scheduleJob)  {
        if (!Strings.isNullOrEmpty(scheduleJob.getCronExpression())) {
            scheduleJob.setJobClass(ReminderCronJob.class.getName());
            scheduleJob.setCronJob(true);
        } else {
            scheduleJob.setJobClass(SimpleCronJob.class.getName());
            scheduleJob.setCronJob(false);
            scheduleJob.setRepeatTime(scheduleJob.getRepeatTime());
        }
        if (scheduleJob.getId() > 0) {
            updateScheduleJob(scheduleJob);
        } else {
            log.info("Job Info: {}", scheduleJob);
            scheduleNewJob(scheduleJob);
        }
        scheduleJob.setDesc("i am job number " + scheduleJob.getId());
        scheduleJob.setInterfaceName("interface_" + scheduleJob.getId());
        log.info(">>>>> jobName = [" + scheduleJob.getJobName() + "]" + " created.");
        return scheduleJob;
    }

    @SuppressWarnings("unchecked")
    private void scheduleNewJob(ECScheduleJobInfo jobInfo) {
        try {
            ECScheduleJobInfo info = ecSchedulerJobInfoRepository.findByJobName(jobInfo.getJobName());
            if (info != null) throw new ECBusinessException("Job name existed in the system");
            Scheduler scheduler = schedulerFactoryBean.getScheduler();
            JobDetail jobDetail = JobBuilder
                    .newJob((Class<? extends QuartzJobBean>) Class.forName(jobInfo.getJobClass()))
                    .withIdentity(jobInfo.getJobName(), jobInfo.getJobGroup()).build();
            if (!scheduler.checkExists(jobDetail.getKey())) {
                jobDetail = scheduleCreator.createJob(
                        (Class<? extends QuartzJobBean>) Class.forName(jobInfo.getJobClass()), false, context,
                        jobInfo.getJobName(), jobInfo.getJobGroup());

                Trigger trigger;
                if (jobInfo.getCronJob()) {
                    trigger = scheduleCreator.createCronTrigger(jobInfo.getJobName(), new Date(),
                            jobInfo.getCronExpression(), SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW);
                } else {
                    trigger = scheduleCreator.createSimpleTrigger(jobInfo.getJobName(), new Date(),
                            jobInfo.getRepeatTime(), SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW);
                }
                scheduler.scheduleJob(jobDetail, trigger);
                jobInfo.setJobStatus("SCHEDULED");
                ecSchedulerJobInfoRepository.save(jobInfo);
                log.info(">>>>> jobName = [" + jobInfo.getJobName() + "]" + " scheduled.");
            } else {
                log.error("scheduleNewJobRequest.jobAlreadyExist");
            }
        } catch (ClassNotFoundException | SchedulerException e) {
            log.error("Class Not Found - {}", jobInfo.getJobClass(), e);
        } catch (ECBusinessException ex) {
            throw ex;
        }
    }

    private void updateScheduleJob(ECScheduleJobInfo jobInfo) {
        Trigger newTrigger;
        ECScheduleJobInfo old = ecSchedulerJobInfoRepository.getById(jobInfo.getId());
        if (old == null) throw new ECBusinessException("Not found job in the system");
        if (!old.getJobName().equalsIgnoreCase(jobInfo.getJobName())) {
            ECScheduleJobInfo info = ecSchedulerJobInfoRepository.findByJobName(jobInfo.getJobName());
            if (info != null) throw new ECBusinessException("Job name existed in the system");
        }
        if (jobInfo.getCronJob()) {
            newTrigger = scheduleCreator.createCronTrigger(jobInfo.getJobName(), new Date(),
                    jobInfo.getCronExpression(), SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW);
        } else {
            newTrigger = scheduleCreator.createSimpleTrigger(jobInfo.getJobName(), new Date(), jobInfo.getRepeatTime(),
                    SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW);
        }
        try {
            schedulerFactoryBean.getScheduler().rescheduleJob(TriggerKey.triggerKey(jobInfo.getJobName()), newTrigger);
            jobInfo.setJobStatus("EDITED & SCHEDULED");
            ecSchedulerJobInfoRepository.save(jobInfo);
            log.info(">>>>> jobName = [" + jobInfo.getJobName() + "]" + " updated and scheduled.");
        } catch (SchedulerException e) {
            log.error(e.getMessage(), e);
        }
    }
}
