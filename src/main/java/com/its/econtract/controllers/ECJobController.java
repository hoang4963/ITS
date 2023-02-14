package com.its.econtract.controllers;

import com.its.econtract.dto.EContractDto;
import com.its.econtract.entity.schedule.ECScheduleJobInfo;
import com.its.econtract.controllers.request.ScheduleRequest;
import com.its.econtract.services.ECSchedulerService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Log4j2
@RestController
@RequestMapping(value = "/api/v1")
public class ECJobController {

    @Autowired
    private ECSchedulerService schedulerService;

    @ApiOperation(value = "Create schedule job")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Create Job successfully"),
            @ApiResponse(code = 400, message = "Input params are not correct", response = String.class),
            @ApiResponse(code = 404, message = "Not found the document or document is deactivated", response = String.class),
            @ApiResponse(code = 415, message = "The content type is unsupported"),
            @ApiResponse(code = 500, message = "An unexpected error has occurred. The error has been logged and is being investigated.")})
    @PostMapping(value = "/job/setup")
    public ResponseEntity createOrUpdateJob(@RequestBody @Valid ScheduleRequest request) {
        ECScheduleJobInfo jobInfo = new ECScheduleJobInfo();
        jobInfo.setDesc(request.getDesc());
        jobInfo.setId(request.getId());
        jobInfo.setJobName(request.getJobName());
        jobInfo.setCronExpression(request.getCronExpression());
        jobInfo.setRepeatTime(request.getRequestTime());
        ECScheduleJobInfo result = schedulerService.saveOrUpdate(jobInfo);
        return EContractDto.build().withData(result).withHttpStatus(HttpStatus.OK).withMessage("Successfully").toResponseEntity();
    }

    @ApiOperation(value = "Get schedule jobs")
    @GetMapping(value = "/jobs")
    public ResponseEntity getAllJobs() {
        List<ECScheduleJobInfo> jobs = schedulerService.getAllJobList();
        return EContractDto.build().withData(jobs).withHttpStatus(HttpStatus.OK).withMessage("Successfully").toResponseEntity();
    }

    @ApiOperation(value = "RunJob")
    @PostMapping(value = "/job/{id}/run")
    public ResponseEntity runJob(@PathVariable int id) {
        boolean jobs = schedulerService.startJobNow(id);
        return EContractDto.build().withData(jobs).withHttpStatus(HttpStatus.OK).withMessage("Successfully").toResponseEntity();
    }

    @ApiOperation(value = "Stop job")
    @PostMapping(value = "/job/{id}/stop")
    public ResponseEntity pauseJob(@PathVariable int id) {
        boolean jobs = schedulerService.pauseJob(id);
        return EContractDto.build().withData(jobs).withHttpStatus(HttpStatus.OK).withMessage("Successfully").toResponseEntity();
    }

    @ApiOperation(value = "Resume job")
    @PostMapping(value = "/job/{id}/resume")
    public ResponseEntity resumeJob(@PathVariable int id) {
        boolean jobs = schedulerService.resumeJob(id);
        return EContractDto.build().withData(jobs).withHttpStatus(HttpStatus.OK).withMessage("Successfully").toResponseEntity();
    }

    @ApiOperation(value = "Delete job")
    @DeleteMapping(value = "/job/{id}")
    public ResponseEntity deleteJob(@PathVariable int id) {
        boolean jobs = schedulerService.deleteJob(id);
        return EContractDto.build().withData(jobs).withHttpStatus(HttpStatus.OK).withMessage("Successfully").toResponseEntity();
    }
}
