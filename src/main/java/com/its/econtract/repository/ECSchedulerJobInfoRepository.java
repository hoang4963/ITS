package com.its.econtract.repository;

import com.its.econtract.entity.schedule.ECScheduleJobInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ECSchedulerJobInfoRepository extends JpaRepository<ECScheduleJobInfo, Integer> {
    ECScheduleJobInfo findByJobName(String jobName);
}
