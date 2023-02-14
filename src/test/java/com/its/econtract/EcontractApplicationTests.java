package com.its.econtract;

import com.mysql.cj.x.protobuf.MysqlxDatatypes;
import org.springframework.core.SpringVersion;

import static org.junit.jupiter.api.Assertions.assertEquals;

//package com.its.econtract;
//
//import com.its.econtract.entity.ECDocuments;
//import com.its.econtract.entity.NotificationTemplate;
//import com.its.econtract.entity.converter.Aes;
//import com.its.econtract.entity.enums.ECDocumentType;
//import com.its.econtract.facade.ECMergeSignDocumentFacade;
//import com.its.econtract.helpers.ECDateUtils;
//import com.its.econtract.controllers.request.SignatureDocumentRequest;
//import com.its.econtract.repository.ECSendEmailRepository;
//import com.its.econtract.services.ECSchedulerService;
////import com.its.econtract.validator.ECValidatorDto;
//import lombok.Getter;
//import lombok.Setter;
//import lombok.extern.log4j.Log4j2;
//import org.apache.commons.codec.binary.Base64;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.thymeleaf.context.Context;
//import org.thymeleaf.spring5.SpringTemplateEngine;
//
//import javax.validation.constraints.Max;
//import javax.validation.constraints.Min;
//import javax.validation.constraints.NotNull;
//
//import java.util.Arrays;
//import java.util.Date;
//import java.util.List;
//
//@Log4j2
//@SpringBootTest
//class EcontractApplicationTests {
//
//    @Autowired
//    private SpringTemplateEngine templateEngine;
//
//    @Test
//    public void testEmailTemplate() {
//        Context context = new Context();
//        context.setVariable("name", "Nguyễn Văn Nam");
//        String template = "Hi [[${name}]]!";
//        String content = templateEngine.process(template, context);
//        System.out.print(content);
//    }
//
////    @Autowired
//    ECSchedulerService schedulerService;
//
////    @Test
////    public void createJob() {
////        ECScheduleJobInfo jobInfo = new ECScheduleJobInfo();
////        jobInfo.setCronJob(false);
////        jobInfo.setJobName("ITS-TEST-REMINDER");
////        jobInfo.setId(-1);
////        jobInfo.setRepeatTime(30L);
////        jobInfo.setDesc("NamNV - using to test");
////        schedulerService.saveOrUpdate(jobInfo);
////    }
//
//    @Autowired
//    NotificationTemplate notificationTemplate;
//
//    @Test
//    public void testGetDocuments() {
//        Date date = ECDateUtils.addDays(new Date(), 5);
//        Date begin = ECDateUtils.setStartTimeOfDay(date);
//        Date end = ECDateUtils.setEndTimeOfDay(date);
//    	List<Integer> listOfStatus = Arrays.asList(ECDocumentType.ChoDuyet.getValue(), ECDocumentType.ChoKySo.getValue());
//        List<ECDocuments> documents = notificationTemplate.getDocumentByDateAndStatus(-1, listOfStatus, begin, end, 50);
//        System.out.println(documents);
//    }
//
////    @Autowired
////    ECValidatorDto<UserDto> ecValidatorDto;
//
//    @Autowired
//    ECSendEmailRepository emailRepository;
//
//    @Test
//    public void testDto() throws Exception {
////        UserDto dto = new UserDto();
////        dto.setAge(10);
////        dto.setEmail("namnvhut@gmail.com");
////        dto.setUsername("namnvhut");
////        String test = Aes.encrypt(Base64.decodeBase64("0iIB7rnoD54fsENE4B7D0xhCu35DG82MX11ThxiE+dM="), "nammai252087");
////        System.out.println(test);
////        ECSendEmail s = emailRepository.getById(1);
////        System.out.println(s.getPassword());
//        System.out.println(new String(Base64.decodeBase64("eyJpdiI6InpYc2pLa1pIc0JOOVZMYnRIMkVJR0E9PSIsInZhbHVlIjoidmVxeGlIVjZEWTlCK0dmaUFoS0Nndz09IiwibWFjIjoiZjcwZjQ5NGUyOTIyYmFjMzg4MTAzMzdhNmFiNjZjZDU5NzVlYTJjNjZkM2RhYmRjYWFmMTIwYmQwZmU4ZDcxYSJ9")));
//        String dec = Aes.decrypt(Base64.decodeBase64("ekgKB7QJWUOSpM98qPlzdLTgzTddhprZZWyvCExwnJI="),
//                "zXsjKkZHsBN9VLbtH2EIGA==", "veqxiHV6DY9B+GfiAhKCgw==",
//                "f70f494e2922bac38810337a6ab66cd5975ea2c66d3dabdcaaf120bd0fe8d71a");
//        System.out.println(dec);
//    }
//
////    [2022-04-05 20:52:53] local.INFO: eyJpdiI6IkZnd0tHZmNoL1VqWnhDRnlya3M2dkE9PSIsInZhbHVlIjoiOEpYUnc3VVI2SFlMU2wyd3IvV3BXdz09IiwibWFjIjoiODEyZGRiYzYyZDc5ODg2YjNkYTcyZWVkNGY2NWQ4OWI1ZjMyYzE4OWUyOGVlYjBjNTA0NWYyZWRhYWJkYTI0MiJ9
////    [2022-04-05 20:52:53] local.INFO: eyJpdiI6IlM3T1pTYVdaSW5wWlJHdEtVaytoeEE9PSIsInZhbHVlIjoiRWZEbkJWTXU2OWp5NDE5aU9zM0g2Zz09IiwibWFjIjoiMTE2ZjYxZjgxMDUwMTgwN2EwMGY0NWE2N2M1MTI4YmRiYTY1NTA1MjhiNDBlOTI5MTc1ODYxZThjNTAxOWM0OCJ9
//    // eyJpdiI6ImtJc0tnMnF4ZXhxSkFLeWRjZ1JmeUFcdTAwM2RcdTAwM2QiLCJ2YWx1ZSI6InlFaHNLWlJ3Zis4YkZCalpNVTNsS3lkU21iMzh0WlVSWHkrN3JsejFUWlFcdTAwM2QiLCJtYWMiOiI1NzY3MjQ3NDg3Y2JmMDNjNzkzYzgyZmNiNzYxZDVmZTQ0YWU1ZGM3YTgyODdmYjZjZjAwZjRhNmYzNjU2NzZmIn0=
//    // eyJpdiI6IjdNYkoydXB0TnJiL3o1a0VMNFZPTHdcdTAwM2RcdTAwM2QiLCJ2YWx1ZSI6IitNQ3RJSTVaUm1aTWE3aXVkc1dRd2dcdTAwM2RcdTAwM2QiLCJtYWMiOiI2ZmZjZWQ1NjFlYzJmNzI5OGM0NTk0NmIwZjcyNzIxMTMxNmVhZGU4ODMwZjJiZDM3NDg0ZWE4NGY5MGJkNzZkIn0=
//    // eyJpdiI6Ik1QZDRWR0FzaW11Z0NTY1daWUZHK3c9PSIsInZhbHVlIjoiNDdXSTVmeUMwS0lPT1ovMm9xYzQvUT09IiwibWFjIjoiY2MxNzhmNzk4MDQwYjFkM2FhMDFlNDdmNTRmY2FiY2U3NzI1ZTUxZGM2MTIzNWI3Y2IyZWNhZWU0ZWM0MjNmOSJ9
//    // eyJpdiI6ImZnZlRpSE1UUlNwYUZTVUF4SmxQY2dcdTAwM2RcdTAwM2QiLCJ2YWx1ZSI6ImhyYXhRMkg3VytmMWNlS2ZuMzNRa094bkJLNWhJVUVReVF3Q2RscWFzdWtcdTAwM2QiLCJtYWMiOiJkMjBlZmIyYTBkNDU0YjQxYmZhN2EyYmNkMDkwZjZkYjM3NmViYzJmNTllZGZjNmVlNzA2MzY0ZmRlYWQ4NTA0In0=
//
//    @Setter
//    @Getter
//    public static class UserDto {
//        @NotNull(message = "username must be required")
//        private String username;
//        @NotNull(message = "email must be required")
//        private String email;
//        @Min(value = 18, message = "Age should not be less than 18")
//        @Max(value = 150, message = "Age should not be greater than 150")
//        private int age;
//    }
//
//
//    @Autowired
//    private ECMergeSignDocumentFacade signDocumentFacade;
//    @Test
//    public void testSignaturePdf() throws Exception {
//        SignatureDocumentRequest signatureDocumentRequest = new SignatureDocumentRequest();
//        signatureDocumentRequest.setDocumentId(1);
//        signatureDocumentRequest.setAssignId(1);
//        signDocumentFacade.signDocument(signatureDocumentRequest);
//    }
//
//}

