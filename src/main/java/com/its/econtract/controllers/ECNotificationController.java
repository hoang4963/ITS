package com.its.econtract.controllers;

import com.its.econtract.controllers.request.NotificationAccRequest;
import com.its.econtract.controllers.request.NotificationReSendMsgRequest;
import com.its.econtract.controllers.request.NotificationRequest;
import com.its.econtract.dto.EContractDto;
import com.its.econtract.entity.ECDocumentSignatureKyc;
import com.its.econtract.entity.ECDocumentTextInfos;
import com.its.econtract.exceptions.ECBusinessException;
import com.its.econtract.facade.ECNotificationFacade;
import com.its.econtract.utils.ECDateUtils;
import com.its.econtract.repository.ECDocumentSignatureKycRepository;
import com.its.econtract.repository.ECDocumentTextInfosRepository;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.schedulers.Schedulers;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping(value = "/api/v1")
@Log4j2
public class ECNotificationController {

    @Autowired
    private ECNotificationFacade facade;


    @PostMapping("notify")
    public Callable<ResponseEntity> sendNotify(@RequestBody @Valid NotificationRequest notificationRequest) {
        return () -> {
            facade.sendMsg(notificationRequest);
            return EContractDto.build().withData(1).withHttpStatus(HttpStatus.OK).withMessage("Successfully").toResponseEntity();
        };
    }

    @PostMapping("notify/account")
    public Callable<ResponseEntity> sendNotifyAcc(@RequestBody @Valid NotificationAccRequest notificationRequest) throws Exception {
        return () -> {
            int result = facade.sendMsgAcc(notificationRequest).get();
            return EContractDto.build().withData(result).withHttpStatus(HttpStatus.OK).withMessage("Successfully").toResponseEntity();
        };
    }

    @PostMapping("notify/re-notify")
    public Callable<ResponseEntity> reSendMsg(@RequestBody @Valid NotificationReSendMsgRequest notify) {
        return () -> {
            int result = facade.reSendNotify(notify).get();
            return EContractDto.build().withData(result).withHttpStatus(HttpStatus.OK).withMessage("Successfully").toResponseEntity();
        };
    }

    @GetMapping("notify")
    public Single<ResponseEntity> checkRxJava(@RequestParam("s") String s) {
        if ("1".equalsIgnoreCase(s))
            return Single.just(new ResponseEntity<>("single value", HttpStatus.CREATED));
        else
            return Single.error(new ECBusinessException("EContract"));
    }



    @RequestMapping(method = RequestMethod.GET, value = "/timeout")
    public Observable<String> timeout() {
        return Observable.timer(1, TimeUnit.MINUTES).map((Function<Long, String>) aLong -> "single value");
    }

    @Autowired
    ECDocumentSignatureKycRepository signatureKycRepository;

    @Autowired
    ECDocumentTextInfosRepository textInfosRepository;

    @Setter
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Data {
        List<ECDocumentSignatureKyc> kyc;
        List<ECDocumentTextInfos> textInfosList;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/checking")
    public Single<ResponseEntity<EContractDto>> getValue() {
        return Single.zip(
                        getKyc(),
                        getTextInfo(), (kyc, info) -> {
                            Data d = new Data();
                            d.setKyc(kyc);
                            d.setTextInfosList(info);
                            return EContractDto.build().withData(d).withHttpStatus(HttpStatus.OK).withMessage("Successfully").toResponseEntity();
                        })
                .subscribeOn(Schedulers.io());
    }

    private Single<List<ECDocumentSignatureKyc>> getKyc() {
        return Single.create(emitter -> {
            try {
                log.info("getKyc: {}", ECDateUtils.currentTimeMillis());
                List<ECDocumentSignatureKyc> kyc = signatureKycRepository.findAll();
                log.info("getKyc: {}", ECDateUtils.currentTimeMillis());
                emitter.onSuccess(kyc);
            } catch (Exception e) {
                emitter.onError(e);
            }
        });
    }

    private Single<List<ECDocumentTextInfos>> getTextInfo() {
        return Single.create(emitter -> {
            try {
                log.info("getTextInfo: {}", ECDateUtils.currentTimeMillis());
                List<ECDocumentTextInfos> kyc = textInfosRepository.findAll();
                emitter.onSuccess(kyc);
                log.info("getTextInfo: {}", ECDateUtils.currentTimeMillis());
            } catch (Exception e) {
                emitter.onError(e);
            }
        });
    }
}
