package com.its.econtract.controllers;

import com.its.econtract.dto.EContractDto;
import com.its.econtract.dto.KYCResponseDto;
import com.its.econtract.dto.OCRBackResponseDto;
import com.its.econtract.dto.OCRFrontResponseDto;
import com.its.econtract.controllers.request.EKycRequest;
import com.its.econtract.controllers.request.KYCRequest;
import com.its.econtract.controllers.request.OCRRequest;
import com.its.econtract.services.OCRService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.io.IOException;

@RestController
@RequestMapping(value = "/api/v1")
public class ECOCRController {
    @Autowired
    private OCRService ocrService;

//    @PostMapping("/orc/front")
//    public ResponseEntity orcFront(@Valid OCRRequest ocrRequest) {
//        OCRFrontResponseDto result = ocrService.ocrFrontResponseDto(ocrRequest);
//        return EContractDto.build()
//                .withData(result)
//                .withHttpStatus(HttpStatus.OK)
//                .withMessage("Successfully")
//                .toResponseEntity();
//    }
//
//    @PostMapping("/orc/back")
//    public ResponseEntity orcBack(@Valid OCRRequest ocrRequest) {
//        OCRBackResponseDto result = ocrService.ocrBackResponseDto(ocrRequest);
//        return EContractDto.build()
//                .withData(result)
//                .withHttpStatus(HttpStatus.OK)
//                .withMessage("Successfully")
//                .toResponseEntity();
//    }
//
//    @PostMapping("/orc/verify")
//    public ResponseEntity orcVerify(@Valid KYCRequest kycRequest) {
//        KYCResponseDto result = ocrService.kycResponseDto(kycRequest);
//        return EContractDto.build()
//                .withData(result)
//                .withHttpStatus(HttpStatus.OK)
//                .withMessage("Successfully")
//                .toResponseEntity();
//    }

    @PostMapping("/orc/ekyc/verify")
    public ResponseEntity orc(@RequestBody @Valid EKycRequest kycRequest) throws IOException {
        Object result = ocrService.eKycVerify(kycRequest);
        return EContractDto.build()
                .withData(result)
                .withHttpStatus(HttpStatus.OK)
                .withMessage("Successfully")
                .toResponseEntity();
    }
}
