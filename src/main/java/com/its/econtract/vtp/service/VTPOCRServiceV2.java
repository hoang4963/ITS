package com.its.econtract.vtp.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.its.econtract.controllers.response.KYCResponse;
import com.its.econtract.controllers.response.OCRResponse;
import com.its.econtract.entity.ECKycLog;
import com.its.econtract.exceptions.ECBusinessException;
import com.its.econtract.utils.ECDateUtils;
import com.its.econtract.repository.ECKycLogRepository;
import com.its.econtract.utils.MessageUtils;
import com.its.econtract.vtp.request.KYCRequestV2;
import com.its.econtract.vtp.request.OCRRequestV2;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;
import java.util.Objects;

@Log4j2
@Component
public class VTPOCRServiceV2 extends VTPClientV2 {
    @Autowired
    MessageUtils messageUtil;

    @Autowired
    private ECKycLogRepository ecKycLogRepository;

    public OCRResponse ocr(String token, OCRRequestV2 ocrRequest, Integer objectId, Integer type, Integer companyId) {
        Response<OCRResponse> response;
        long startTime = ECDateUtils.currentTimeMillis();
        try {
            Call<OCRResponse> request = vtpCommunicate.ocr(token, ocrRequest);
            response = request.execute();
            long endTime = ECDateUtils.currentTimeMillis();
            ECKycLog log_ = buildKycLog(startTime, endTime, response, objectId, type, companyId);
            if (log_ != null) ecKycLogRepository.save(log_);
            log.info("Response {} executionTime = {}", response, (endTime - startTime));
            if (response.isSuccessful()) {
                OCRResponse dto = response.body();
                if (null == dto) {
                    log.error("Process face match. Response body null");
                    throw new ECBusinessException(MessageUtils.OCR, HttpStatus.INTERNAL_SERVER_ERROR);
                }
                log.info("Process face match result {}", dto);
                return dto;
            } else {
                String errBody = response.errorBody() == null ? "" : response.errorBody().string();
                log.error("Process face match : code = {}, message={}, response={}", response.code(), response.message(), errBody);
                throw new ECBusinessException(MessageUtils.OCR, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (Exception ex) {
            log.error("Error cause", ex);
            throw new ECBusinessException(MessageUtils.OCR, HttpStatus.INTERNAL_SERVER_ERROR);
        } finally {
            log.info("Process face match in {} ms", (ECDateUtils.currentTimeMillis() - startTime));
        }
    }

    public KYCResponse verify(String token, KYCRequestV2 vtpKycRequest, Integer objectId, Integer type, Integer companyId) {
        Response<KYCResponse> response;
        long startTime = ECDateUtils.currentTimeMillis();
        try {
            Call<KYCResponse> request = vtpCommunicate.verify(token, vtpKycRequest);
            response = request.execute();
            long endTime = ECDateUtils.currentTimeMillis();
            ECKycLog log_ = buildKycLog(startTime, endTime, response, objectId, type, companyId);
            if (log_ != null) ecKycLogRepository.save(log_);
            log.info("Response {} executionTime = {}", response, (endTime - startTime));
            if (response.isSuccessful()) {
                KYCResponse dto = response.body();
                if (null == dto) {
                    log.error("Process face match. Response body null");
                    throw new ECBusinessException(MessageUtils.FACE_MATCH, HttpStatus.INTERNAL_SERVER_ERROR);
                }
                log.info("Process face match result {}", dto);
                return dto;
            } else {
                String errBody = response.errorBody() == null ? "" : response.errorBody().string();
                log.error("Process face match : code = {}, message={}, response={}", response.code(), response.message(), errBody);
                throw new ECBusinessException(MessageUtils.FACE_MATCH, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (IOException ex) {
            log.error("Error cause:", ex);
            throw new ECBusinessException(MessageUtils.FACE_MATCH, HttpStatus.INTERNAL_SERVER_ERROR);
        } finally {
            log.info("Process face match in {}ms", (ECDateUtils.currentTimeMillis() - startTime));
        }
    }

    private ECKycLog buildKycLog(long startTime, long endTime, Response<?> response, Integer documentId, Integer type, Integer companyId) {
        try {
            ECKycLog ecKycLog = new ECKycLog();
            ecKycLog.setCode(response.code());
            ecKycLog.setStartTime(startTime);
            ecKycLog.setEndTime(endTime);
            ecKycLog.setApiPath(String.valueOf(Objects.requireNonNull(response.raw().networkResponse()).request().url()));
            if (response.code() == 200) {
                if (response.body() != null) {
                    ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
                    String json = ow.writeValueAsString(response.body());
                    ecKycLog.setContent(json);
                }
            } else {
                if (response.errorBody() != null) {
                    ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
                    String json = ow.writeValueAsString(response.errorBody());
                    ecKycLog.setContent(json);
                }
            }
            ecKycLog.setVendor("VVN");
            ecKycLog.setObjectId(documentId);
            ecKycLog.setKycType(type);
            ecKycLog.setCompanyId(companyId);
            return ecKycLog;
        } catch (JsonProcessingException e) {
            log.error("buildKycLog cause: ", e);
            return null;
        }
    }
}
