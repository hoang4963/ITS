package com.its.econtract.vtp.controller;


import com.its.econtract.controllers.request.EKycRequest;
import com.its.econtract.dto.EContractDto;
import com.its.econtract.vtp.facade.VTPEkycFacadeV2;
import com.its.econtract.vtp.request.CheckImageRequest;
import com.its.econtract.vtp.request.ECOCRRequest;
import com.its.econtract.vtp.response.*;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;


@RestController
@RequestMapping(value = "/api/v1")
public class VTPEKYCController {
    @Autowired
    private VTPEkycFacadeV2 ecEkycFacade;

    @PostMapping("/orc/verify/vtp")
    public ResponseEntity verify(@Valid @RequestBody EKycRequest kycRequest) {
        VTPKycResponse result = ecEkycFacade.verify(kycRequest);
        return EContractDto.build()
                .withData(result)
                .withHttpStatus(HttpStatus.OK)
                .withMessage("Successfully")
                .toResponseEntity();
    }

    @PostMapping("/orc/check")
    public ResponseEntity checkImage(@Valid @RequestBody CheckImageRequest checkImageRequest) {
        BaseResponse result = ecEkycFacade.checkImage(checkImageRequest);
        return EContractDto.build()
                .withData(result)
                .withHttpStatus(HttpStatus.OK)
                .withMessage("Successfully")
                .toResponseEntity();
    }

    @PostMapping("/orc/ocr")
    public ResponseEntity orc(@Valid @RequestBody ECOCRRequest ecocrRequest) throws Exception  {
        VTPOCRResponse result = ecEkycFacade.ocr(ecocrRequest);
        return EContractDto.build()
                .withData(result)
                .withHttpStatus(HttpStatus.OK)
                .withMessage("Successfully")
                .toResponseEntity();
    }


}
