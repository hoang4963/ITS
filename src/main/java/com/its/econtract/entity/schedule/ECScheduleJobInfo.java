package com.its.econtract.entity.schedule;

import com.its.econtract.entity.ECBaseEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Setter
@Getter
@Entity
@Table(name = "ec_scheduler_job_info")
public class ECScheduleJobInfo extends ECBaseEntity {
    @Column(name = "job_name")
    private String jobName;
    @Column(name = "job_group")
    private String jobGroup;
    @Column(name = "job_status")
    private String jobStatus;
    @Column(name = "job_class")
    private String jobClass;
    @Column(name = "cron_expression")
    private String cronExpression;
    @Column(name = "description")
    private String desc;
    @Column(name = "interface_name")
    private String interfaceName;
    @Column(name = "repeat_time")
    private Long repeatTime;
    @Column(name = "cron_job")
    private Boolean cronJob;
}
