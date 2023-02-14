package com.its.econtract.controllers;

import com.google.common.base.Strings;
import com.its.econtract.signature.cloudca.ca.CredentialsListRequestBO;
import com.its.econtract.signature.cloudca.response.CredentialsListResponse;
import com.its.econtract.controllers.request.HashDocumentRequest;
import com.its.econtract.controllers.request.SampleRequest;
import com.its.econtract.controllers.request.SignatureDocumentRequest;
import com.its.econtract.controllers.request.TextDocumentRequest;
import com.its.econtract.controllers.request.UploadDocumentRequest;
import com.its.econtract.dto.EContractDto;
import com.its.econtract.entity.enums.ECSignAction;
import com.its.econtract.entity.enums.ECSignType;
import com.its.econtract.facade.ECConvertDocumentFacade;
import com.its.econtract.facade.ECMergeSignDocumentFacade;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@Log4j2
@RestController
@RequestMapping(value = "/api/v1")
public class ECSignDocumentController {
    @Autowired
    private ECMergeSignDocumentFacade signDocumentFacade;

    @Autowired
    private ECConvertDocumentFacade facade;

    @ApiOperation(value = "Merge signature and text note requests")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Merge file process successfully."),
            @ApiResponse(code = 400, message = "Input params are not correct", response = String.class),
            @ApiResponse(code = 404, message = "Not found the document or document is deactivated", response = String.class),
            @ApiResponse(code = 415, message = "The content type is unsupported"),
            @ApiResponse(code = 500, message = "An unexpected error has occurred. The error has been logged and is being investigated.")})
    @PostMapping(value = "/document/sign")
    public ResponseEntity signatureDocument(@RequestBody @Valid SignatureDocumentRequest documentRequest) throws Exception {
        if (documentRequest.getDocumentId() <= 0)
            return EContractDto.build().withHttpStatus(HttpStatus.BAD_REQUEST).withMessage("Input params is not correct").toResponseEntity();
        if (documentRequest.getAssignId() <= 0) {
            return EContractDto.build().withHttpStatus(HttpStatus.BAD_REQUEST).withMessage("Input params is not correct").toResponseEntity();
        }
        ECSignAction ac = ECSignAction.getSignAction(documentRequest.getSignAction());
        ECSignType signType = ECSignType.getSignType(documentRequest.getSignType());
        if (ac == null || signType == null) {
            return EContractDto.build().withHttpStatus(HttpStatus.BAD_REQUEST).withMessage("Input params is not correct").toResponseEntity();
        }
        if (signType == ECSignType.E_CA_SIGN && Strings.isNullOrEmpty(documentRequest.getCa())) {
            return EContractDto.build().withHttpStatus(HttpStatus.BAD_REQUEST).withMessage("CA meed to provide CA with this signType").toResponseEntity();
        }

        String data = signDocumentFacade.signDocument(documentRequest).get();
        return EContractDto.build().withData(data).withHttpStatus(HttpStatus.OK).toResponseEntity();
    }

    @ApiOperation(value = "Digest documentation content")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Merge file process successfully."),
            @ApiResponse(code = 400, message = "Input params are not correct", response = String.class),
            @ApiResponse(code = 404, message = "Not found the document or document is deactivated", response = String.class),
            @ApiResponse(code = 415, message = "The content type is unsupported"),
            @ApiResponse(code = 500, message = "An unexpected error has occurred. The error has been logged and is being investigated.")})
    @PostMapping(value = "/document/hash-v3")
    public ResponseEntity hashDocument(@RequestBody @Valid HashDocumentRequest hashDocumentRequest) throws Exception {
        if (hashDocumentRequest.getDocumentId() <= 0)
            return EContractDto.build().withHttpStatus(HttpStatus.BAD_REQUEST).withMessage("Input params is not correct").toResponseEntity();

        String data = signDocumentFacade.hashDocument(hashDocumentRequest);
        return EContractDto.build().withData(data).withHttpStatus(HttpStatus.OK).toResponseEntity();
    }

    @ApiOperation(value = "Digest documentation content")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Merge file process successfully."),
            @ApiResponse(code = 400, message = "Input params are not correct", response = String.class),
            @ApiResponse(code = 404, message = "Not found the document or document is deactivated", response = String.class),
            @ApiResponse(code = 415, message = "The content type is unsupported"),
            @ApiResponse(code = 500, message = "An unexpected error has occurred. The error has been logged and is being investigated.")})
    @PostMapping(value = "/document/hash")
    public ResponseEntity hashDocumentV2(@RequestBody @Valid HashDocumentRequest hashDocumentRequest) throws Exception {
        if (hashDocumentRequest.getDocumentId() <= 0)
            return EContractDto.build().withHttpStatus(HttpStatus.BAD_REQUEST).withMessage("Input params is not correct").toResponseEntity();

        String data = signDocumentFacade.hashDocumentV2(hashDocumentRequest);
        return EContractDto.build().withData(data).withHttpStatus(HttpStatus.OK).toResponseEntity();
    }

    @PostMapping(value = "/upload/sample")
    public ResponseEntity uploadFiles1(@Valid UploadDocumentRequest file) throws Exception {
        String packages = facade.convertPDFSample(file.getFile()).get();
        return EContractDto.build().withData(packages).withHttpStatus(HttpStatus.OK).toResponseEntity();
    }

    @ApiOperation(value = "Merge document requests")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Merge file process successfully."),
            @ApiResponse(code = 400, message = "Input params are not correct", response = String.class),
            @ApiResponse(code = 404, message = "Not found the document or document is deactivated", response = String.class),
            @ApiResponse(code = 415, message = "The content type is unsupported"),
            @ApiResponse(code = 500, message = "An unexpected error has occurred. The error has been logged and is being investigated.")})
    @PostMapping(value = "/document/merge/sample")
    public ResponseEntity mergeDocument1(@RequestBody @Valid SampleRequest sampleRequest) throws Exception {
        String data = facade.convertDocumentByIdSample(sampleRequest).get();
        return EContractDto.build().withData(data).withHttpStatus(HttpStatus.OK).toResponseEntity();
    }

    @ApiOperation(value = "Text note requests")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Merge file process successfully."),
            @ApiResponse(code = 400, message = "Input params are not correct", response = String.class),
            @ApiResponse(code = 404, message = "Not found the document or document is deactivated", response = String.class),
            @ApiResponse(code = 415, message = "The content type is unsupported"),
            @ApiResponse(code = 500, message = "An unexpected error has occurred. The error has been logged and is being investigated.")})
    @PostMapping(value = "/document/sign-text")
    public ResponseEntity insertTextDocument(@RequestBody @Valid TextDocumentRequest textDocumentRequest) throws Exception {
        if (textDocumentRequest.getDocumentId() <= 0)
            return EContractDto.build().withHttpStatus(HttpStatus.BAD_REQUEST).withMessage("Input params is not correct").toResponseEntity();

        String data = signDocumentFacade.bindTextDocument(textDocumentRequest).get();
        return EContractDto.build().withData(data).withHttpStatus(HttpStatus.OK).toResponseEntity();
    }

    @PostMapping("/get-list-credentials")
    public ResponseEntity getCredentials(@Valid @RequestBody CredentialsListRequestBO requestBO) {
        List<CredentialsListResponse> result = signDocumentFacade.getListCredentials(requestBO);
        return EContractDto.build()
                .withData(result)
                .withHttpStatus(HttpStatus.OK)
                .withMessage("Successfully")
                .toResponseEntity();
    }
}
