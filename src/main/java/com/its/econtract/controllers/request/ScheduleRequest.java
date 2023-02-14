package com.its.econtract.controllers.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotEmpty;

@Setter
@Getter
@ToString
public class ScheduleRequest {

    @JsonProperty(value = "id")
    @ApiModelProperty(name = "job_name", example = "ITS-REMINDER-SMS", required = true)
    private Integer id;

    @NotEmpty(message = "Job name is required")
    @JsonProperty(value = "job_name")
    @ApiModelProperty(name = "job_name", example = "ITS-REMINDER-SMS", required = true)
    private String jobName;

    @JsonProperty(value = "cron_expression")
    @ApiModelProperty(name = "cronExpression", example = "0/5 * * * *", required = true)
    private String cronExpression = "";

    @JsonProperty(value = "description")
    @ApiModelProperty(name = "description", example = "CronJob", required = true)
    private String desc;

    @JsonProperty(value = "repeat_time")
    private long requestTime;
}
