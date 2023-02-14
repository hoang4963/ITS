package com.its.econtract.vtp.service;


import com.its.econtract.exceptions.ECBusinessException;
import com.its.econtract.utils.ECDateUtils;
import com.its.econtract.utils.MessageUtils;
import com.its.econtract.vtp.request.*;
import com.its.econtract.vtp.response.AuthenticationResponse;
import com.its.econtract.vtp.response.BaseResponse;
import com.its.econtract.vtp.response.VTPKycResponse;
import com.its.econtract.vtp.response.VTPOCRResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;
import java.util.Date;

@Log4j2
@Component
public class VTPOCRService extends VTPClient {
    @Autowired
    MessageUtils messageUtil;

    /**
     * get token
     * @param vtpAuthenticationRequest
     * @return
     */
    public AuthenticationResponse auth(VTPAuthenticationRequest vtpAuthenticationRequest){
        Response<AuthenticationResponse> response;
        long startTime = ECDateUtils.currentTimeMillis();
        try{
            Call<AuthenticationResponse> request = vtpCommunicate.authentication(vtpAuthenticationRequest);
            response = request.execute();
            log.info("Response {}", response);
            if (response.isSuccessful()) {
                AuthenticationResponse dto = response.body();
                if (null == dto) {
                    log.error("Process check authentication. Response body null");
                    throw new ECBusinessException(MessageUtils.AUTH, HttpStatus.INTERNAL_SERVER_ERROR);
                }
                log.info("Process check Authentication result {}", dto);
                return dto;
            } else {
                String errBody = response.errorBody() == null ? "" : response.errorBody().string();
                log.error("Process check Authentication : code = {}, message={}, response={}", response.code(), response.message(), errBody);
                throw new ECBusinessException(MessageUtils.AUTH, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }catch (IOException ex) {
            log.info("Error {}", new Date(startTime));
            throw new ECBusinessException(MessageUtils.AUTH, HttpStatus.INTERNAL_SERVER_ERROR);
        } finally {
            log.info("Authentication in {}ms", (ECDateUtils.currentTimeMillis() - startTime));
        }
    }

    /**
     * so độ khớp khuôn mặt
     * @param token
     * @param vtpKycRequest
     * @return
     */
    public VTPKycResponse verify(String token, VTPKycRequest vtpKycRequest){
        Response<VTPKycResponse> response;
        long startTime = ECDateUtils.currentTimeMillis();
        try{
            Call<VTPKycResponse> request = vtpCommunicate.verify(token,vtpKycRequest);
            response = request.execute();
            log.info("Response {}", response);
            if (response.isSuccessful()) {
                VTPKycResponse dto = response.body();
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
        }catch (IOException ex) {
            log.info("Error {}", new Date(startTime));
            throw new ECBusinessException(MessageUtils.FACE_MATCH, HttpStatus.INTERNAL_SERVER_ERROR);
        } finally {
            log.info("Process face match in {}ms", (ECDateUtils.currentTimeMillis() - startTime));
        }
    }

    /**
     * kiểm tra chất lượng giấy tờ tùy thân
     * @param token
     * @param vtpIdentificationCheckRequest
     * @return
     */
    public BaseResponse qualityCheck(String token, VTPIdentificationCheckRequest vtpIdentificationCheckRequest){
        Response<BaseResponse> response;
        long startTime = ECDateUtils.currentTimeMillis();
        try{
            Call<BaseResponse> request = vtpCommunicate.qualityCheck(token, vtpIdentificationCheckRequest);
            response = request.execute();
            log.info("Response {}", response);
            if (response.isSuccessful()) {
                BaseResponse dto = response.body();
                if (null == dto) {
                    log.error("Process quality check. Response body null");
                    throw new ECBusinessException(MessageUtils.QUALITY_CHECK, HttpStatus.INTERNAL_SERVER_ERROR);
                }
                log.info("Process quality check result {}", dto);
                return dto;
            } else {
                String errBody = response.errorBody() == null ? "" : response.errorBody().string();
                log.error("Process quality check : code = {}, message={}, response={}", response.code(), response.message(), errBody);
                throw new ECBusinessException(MessageUtils.QUALITY_CHECK, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }catch (IOException ex) {
            log.info("Error {}", new Date(startTime));
            throw new ECBusinessException(MessageUtils.QUALITY_CHECK, HttpStatus.INTERNAL_SERVER_ERROR);
        } finally {
            log.info("Quality check in {}ms", (ECDateUtils.currentTimeMillis() - startTime));
        }
    }

    /**
     * kiểm tra giả mạo giấy tờ tùy thân
     * @param token
     * @param vtpIdentificationCheckRequest
     * @return
     */
    public BaseResponse spoofCheck(String token, VTPIdentificationCheckRequest vtpIdentificationCheckRequest){
        Response<BaseResponse> response;
        long startTime = ECDateUtils.currentTimeMillis();
        try{
            Call<BaseResponse> request = vtpCommunicate.spoofCheck(token, vtpIdentificationCheckRequest);
            response = request.execute();
            log.info("Response {}", response);
            if (response.isSuccessful()) {
                BaseResponse dto = response.body();
                if (null == dto) {
                    log.error("Process spoof check. Response body null");
                    throw new ECBusinessException(MessageUtils.SPOOF_CHECK, HttpStatus.INTERNAL_SERVER_ERROR);
                }
                log.info("Process spoof check result {}", dto);
                return dto;
            } else {
                String errBody = response.errorBody() == null ? "" : response.errorBody().string();
                log.error("Process spoof check : code = {}, message={}, response={}", response.code(), response.message(), errBody);
                throw new ECBusinessException(MessageUtils.SPOOF_CHECK, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }catch (IOException ex) {
            log.info("Error {}", new Date(startTime));
            throw new ECBusinessException(MessageUtils.SPOOF_CHECK, HttpStatus.INTERNAL_SERVER_ERROR);
        } finally {
            log.info("Spoof check in {}ms", (ECDateUtils.currentTimeMillis() - startTime));
        }
    }

    /**
     * kiểm tra chất lượng ảnh live
     * @param token
     * @param vtpIdentificationCheckRequest
     * @return
     */
    public BaseResponse faceQualityCheck(String token, VTPIdentificationCheckRequest vtpIdentificationCheckRequest){
        Response<BaseResponse> response;
        long startTime = ECDateUtils.currentTimeMillis();
        try{
            Call<BaseResponse> request = vtpCommunicate.faceQualityCheck(token, vtpIdentificationCheckRequest);
            response = request.execute();
            log.info("Response {}", response);
            if (response.isSuccessful()) {
                BaseResponse dto = response.body();
                if (null == dto) {
                    log.error("Process Face quality check. Response body null");
                    throw new ECBusinessException(MessageUtils.FACE_QUALITY_CHECK, HttpStatus.INTERNAL_SERVER_ERROR);
                }
                log.info("Process face quality check result {}", dto);
                return dto;
            } else {
                String errBody = response.errorBody() == null ? "" : response.errorBody().string();
                log.error("Process face quality check : code = {}, message={}, response={}", response.code(), response.message(), errBody);
                throw new ECBusinessException(MessageUtils.FACE_QUALITY_CHECK, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }catch (IOException ex) {
            log.info("Error {}", new Date(startTime));
            throw new ECBusinessException(MessageUtils.FACE_QUALITY_CHECK, HttpStatus.INTERNAL_SERVER_ERROR);
        } finally {
            log.info("Face quality check in {}ms", (ECDateUtils.currentTimeMillis() - startTime));
        }
    }

    /**
     * kiểm tra giả mạo ảnh live
     * @param token
     * @param vtpIdentificationCheckRequest
     * @return
     */
    public VTPKycResponse faceLiveness(String token, VTPIdentificationCheckRequest vtpIdentificationCheckRequest){
        Response<VTPKycResponse> response;
        long startTime = ECDateUtils.currentTimeMillis();
        try{
            Call<VTPKycResponse> request = vtpCommunicate.faceLiveness(token, vtpIdentificationCheckRequest);
            response = request.execute();
            log.info("Response {}", response);
            if (response.isSuccessful()) {
                VTPKycResponse dto = response.body();
                if (null == dto) {
                    log.error("Process Face liveness. Response body null");
                    throw new ECBusinessException(MessageUtils.FACE_LIVENESS, HttpStatus.INTERNAL_SERVER_ERROR);
                }
                log.info("Process Face liveness result {}", dto);
                return dto;
            } else {
                String errBody = response.errorBody() == null ? "" : response.errorBody().string();
                log.error("Process Face liveness : code = {}, message={}, response={}", response.code(), response.message(), errBody);
                throw new ECBusinessException(MessageUtils.FACE_LIVENESS, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }catch (IOException ex) {
            log.info("Error {}", new Date(startTime));
            throw new ECBusinessException(MessageUtils.FACE_LIVENESS, HttpStatus.INTERNAL_SERVER_ERROR);
        } finally {
            log.info("Face liveness in {}ms", (ECDateUtils.currentTimeMillis() - startTime));
        }
    }

    /**
     * bóc tách thông tin ocr
     * @param token
     * @param vtpocrRequest
     * @return
     */
    public VTPOCRResponse ocr(String token, VTPOCRRequest vtpocrRequest){
        Response<VTPOCRResponse> response;
        long startTime = ECDateUtils.currentTimeMillis();
        try{
            Call<VTPOCRResponse> request = vtpCommunicate.ocr(token,vtpocrRequest);
            response = request.execute();
            log.info("Response {}", response);
            if (response.isSuccessful()) {
                VTPOCRResponse dto = response.body();
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
        }catch (IOException ex) {
            log.info("Error {}", new Date(startTime));
            throw new ECBusinessException(MessageUtils.OCR, HttpStatus.INTERNAL_SERVER_ERROR);
        } finally {
            log.info("Process face match in {}ms", (ECDateUtils.currentTimeMillis() - startTime));
        }
    }


    /**
     * kiểm trả giả mạo 2 ảnh
     * @param token
     * @param spoofCheckTwoImageRequest
     * @return
     */
    public BaseResponse spoofCheckTwoImage(String token, VTPSpoofCheckTwoImageRequest spoofCheckTwoImageRequest){
        Response<BaseResponse> response;
        long startTime = ECDateUtils.currentTimeMillis();
        try{
            Call<BaseResponse> request = vtpCommunicate.spoofCheckTwoImage(token,spoofCheckTwoImageRequest);
            response = request.execute();
            log.info("Response {}", response);
            if (response.isSuccessful()) {
                BaseResponse dto = response.body();
                if (null == dto) {
                    log.error("Process spoof check two image. Response body null");
                    throw new ECBusinessException(MessageUtils.SPOOF_CHECK_TWO_IMAGE, HttpStatus.INTERNAL_SERVER_ERROR);
                }
                log.info("Process face match result {}", dto);
                return dto;
            } else {
                String errBody = response.errorBody() == null ? "" : response.errorBody().string();
                log.error("Process spoof check two image : code = {}, message={}, response={}", response.code(), response.message(), errBody);
                throw new ECBusinessException(MessageUtils.SPOOF_CHECK_TWO_IMAGE, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }catch (IOException ex) {
            log.info("Error {}", new Date(startTime));
            throw new ECBusinessException(MessageUtils.SPOOF_CHECK_TWO_IMAGE, HttpStatus.INTERNAL_SERVER_ERROR);
        } finally {
            log.info("Process spoof check two image in {}ms", (ECDateUtils.currentTimeMillis() - startTime));
        }
    }
}
