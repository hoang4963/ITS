/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.its.econtract.services.communication.vvn;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.its.econtract.entity.ECKycLog;
import com.its.econtract.exceptions.ECBusinessException;
import com.its.econtract.utils.ECDateUtils;
import com.its.econtract.repository.ECKycLogRepository;
import com.its.econtract.utils.MessageUtils;
import com.its.econtract.controllers.response.KYCResponse;
import com.its.econtract.controllers.response.OCRResponse;
import lombok.extern.log4j.Log4j2;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;
import java.util.Date;
import java.util.Objects;

@Log4j2
@Component
public class VVNOCRService extends VVNClient {

    @Autowired
    private ECKycLogRepository ecKycLogRepository;

    public OCRResponse ocr(String sessionId, byte[] images, String imageName, Integer objectId, Integer type, Integer companyId) {
        Response<OCRResponse> response;
        String requestId = getRequestId(sessionId);
        long startTime = ECDateUtils.currentTimeMillis();
        try {
            RequestBody filePart = RequestBody.create(images, MediaType.parse("multipart/form-data"));
            MultipartBody.Part filePartBody = MultipartBody.Part.createFormData("image", imageName, filePart);
            RequestBody sidPart = RequestBody.create(requestId, MediaType.parse("text/plain"));
            Call<OCRResponse> request = vvnCommunicate.ocrRegconition(token, sidPart, filePartBody);
            response = request.execute();
            long endTime = ECDateUtils.currentTimeMillis();
            ECKycLog log_ = buildKycLog(startTime, endTime, response, objectId, type, companyId);
            if (log_ != null) ecKycLogRepository.save(log_);
            log.info("Response {} executionTime = {}", response, (endTime - startTime));
            if (response.isSuccessful()) {
                OCRResponse dto = response.body();
                if (null == dto) {
                    log.error("Process check image request ID {}. Response body null", requestId);
                    throw new ECBusinessException(messageUtil.getMessage(MessageUtils.SYS_ERR), HttpStatus.INTERNAL_SERVER_ERROR);
                }
                log.info("Process result {}", dto);
                return dto;
            } else {
                String errBody = response.errorBody() == null ? "" : response.errorBody().string();
                log.error("Process check image sid {}: code = {}, message={}, response={}", requestId, response.code(), response.message(), errBody);
                throw new ECBusinessException(messageUtil.getMessage(MessageUtils.SYS_ERR), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (IOException ex) {
            log.error("Process check image sid {}. Response null", requestId);
            log.info("{}|{}|{}|{}", "ocr", requestId, new Date(startTime), null);
            throw new ECBusinessException(messageUtil.getMessage(MessageUtils.SYS_ERR),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        } finally {
            log.info("Verify OCR {} in {}ms", requestId, (ECDateUtils.currentTimeMillis() - startTime));
        }
    }

    public KYCResponse verify(String sessionId, byte[] idImages, String idImage,
                              byte[] faceImages, String faceImage, Integer objectId, Integer type, Integer companyId) {
        long startTime = ECDateUtils.currentTimeMillis();
        String requestId = null;
        try {
            RequestBody filePartIdImage = RequestBody.create(idImages, MediaType.parse("multipart/form-data"));
            MultipartBody.Part filePartBodyIdImage = MultipartBody.Part.createFormData("image_card", idImage, filePartIdImage);

            RequestBody filePartFaceImage = RequestBody.create(faceImages, MediaType.parse("multipart/form-data"));
            MultipartBody.Part filePartBodyFaceImage = MultipartBody.Part.createFormData("image_live", faceImage, filePartFaceImage);
            requestId = getRequestId(sessionId);
            RequestBody sidPart = RequestBody.create(requestId, MediaType.parse("text/plain"));
            RequestBody returnFeaturePart = RequestBody.create("0", MediaType.parse("text/plain"));

            Call<KYCResponse> request = vvnCommunicate.faceVerify(token, sidPart, returnFeaturePart, filePartBodyIdImage, filePartBodyFaceImage);
            Response<KYCResponse> response = request.execute();
            long endTime = ECDateUtils.currentTimeMillis();
            ECKycLog log_ = buildKycLog(startTime, endTime, response, objectId, type, companyId);
            if (log_ != null) ecKycLogRepository.save(log_);
            log.info("Response {} executionTime = {}", response, (endTime - startTime));
            if (response.isSuccessful()) {
                KYCResponse obj = response.body();
                log.info("RequestID: {} FaceVerifyResponse = {}", requestId, obj);
                return obj;
            } else {
                throw new ECBusinessException(
                        messageUtil.getMessage(MessageUtils.SYS_ERR),
                        HttpStatus.INTERNAL_SERVER_ERROR);
            }

        } catch (Exception e) {
            log.error("faceFileVerify ", e);
            throw new ECBusinessException(
                    messageUtil.getMessage(MessageUtils.SYS_ERR),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        } finally {
            log.info("faceFileVerify {} in {}ms", requestId, (ECDateUtils.currentTimeMillis() - startTime));
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
