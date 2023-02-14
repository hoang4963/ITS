package com.its.econtract.controllers;

import com.google.gson.Gson;
import com.its.econtract.controllers.request.ECTrucRequest;
import com.its.econtract.controllers.response.ECVerifyResponse;
import com.its.econtract.dto.EContractDto;
import com.its.econtract.entity.ECTrucFeedback;
import com.its.econtract.entity.enums.ETrucFeedbackState;
import com.its.econtract.utils.ECDateUtils;
import com.its.econtract.repository.ECTrucFeedbackRepository;
import com.its.econtract.services.ECVerifyService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Log4j2
@RestController
@RequestMapping("/api/v1")
public class ECeVerifyListener {

    @Value("${senderId}")
    private String senderId;

    @Value("${receiverId}")
    private String receiverId;

    @Autowired
    private ECTrucFeedbackRepository trucFeedbackRepository;

    private final Gson gson = new Gson();

    @Autowired
    private ECVerifyService verifyService;

    @PostMapping(value = "/e-verify/listener")
    public ECVerifyResponse listener(@RequestBody @Valid ECTrucRequest request) {
        log.info("request: {}", request);
        log.info("[listener] request: {}", gson.toJson(request));
        ECVerifyResponse response = new ECVerifyResponse();
        ECVerifyResponse.ECVerifyInfo info = new ECVerifyResponse.ECVerifyInfo();
        info.setVersion("1.0.0");
        info.setSenderId(senderId);
        info.setReceiverId(receiverId);

        info.setMessageId(request.getInfo().getMessageId());
        info.setMessageType(request.getInfo().getMessageType());
        info.setReceiverId(request.getInfo().getSenderId());
        info.setSenderId(request.getInfo().getReceiverId());
        info.setResponseCode(0);
        info.setResponseMessage("Thành công");
        info.setSendDate(ECDateUtils.currentTimeMillis());

        if (!verifyService.verifySignature(request)) {
            log.warn("Du lieu khong toan ven");
            info.setResponseCode(1);
            info.setResponseMessage("Dữ liệu không toàn vẹn");
            response.setInfo(info);
            verifyService.storeTrucLog(request, request.getContent().getTransactionId());
            return response;
        } else {
            response.setInfo(info);
            verifyService.storeTrucLog(request, request.getContent().getTransactionId());
        }

        ECTrucFeedback trucFeedback = new ECTrucFeedback();
        trucFeedback.setTransactionId(request.getContent().getTransactionId());
        trucFeedback.setVerificationCode(request.getContent().getData().getVerificationCode());
        trucFeedback.setState(ETrucFeedbackState.E_TRUC_FEEDBACK_STATE_OPEN.getValue());
        trucFeedback.setContent(gson.toJson(request));
        trucFeedbackRepository.save(trucFeedback);
        log.info("execute running truc feedback: {}", request.getInfo());
        verifyService.trucFeedbackConfirm(request);

        ECVerifyResponse.ECVerifyContent content = new ECVerifyResponse.ECVerifyContent();
        Map<String, String> data = new ConcurrentHashMap<>();
        data.put("transactionId", request.getContent().getTransactionId());
        content.setData(data);
        response.setContent(content);
        return response;
    }

    @PostMapping(value = "/e-verify/ceca")
    public ResponseEntity uploadCECA(@RequestPart("file") MultipartFile request) throws IOException {
        String transactionId = request.getOriginalFilename().replace(".pdf", "");
        log.info("uploadCECA |=================>> {}", request.getOriginalFilename());
        verifyService.storeTrucHistory(transactionId, request);
        return EContractDto.build().withData("OK").withHttpStatus(HttpStatus.OK).toResponseEntity();
    }
}
