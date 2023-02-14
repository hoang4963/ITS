package com.its.econtract.signature.cloudca.communicate;

import com.google.gson.Gson;
import com.its.econtract.exceptions.ECBusinessException;
import com.its.econtract.utils.ECDateUtils;
import com.its.econtract.signature.cloudca.ca.CredentialsAuthorizeRequestBO;
import com.its.econtract.signature.cloudca.ca.CredentialsAuthorizeResponceBO;
import com.its.econtract.signature.cloudca.ca.CredentialsInfoRequestBO;
import com.its.econtract.signature.cloudca.ca.CredentialsInfoResponceBO;
import com.its.econtract.signature.cloudca.ca.CredentialsListRequestBO;
import com.its.econtract.signature.cloudca.ca.CredentialsListResponceBO;
import com.its.econtract.signature.cloudca.ca.LoginRequestBO;
import com.its.econtract.signature.cloudca.ca.LoginResponceBO;
import com.its.econtract.signature.cloudca.ca.ResponceBO;
import com.its.econtract.signature.cloudca.ca.SignHashRequestBO;
import com.its.econtract.signature.cloudca.ca.SignHashResponceBO;
import com.its.econtract.utils.MessageUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;
import java.util.Date;

@Log4j2
@Component
public class CloudCAService extends CloudCAClient {

    public LoginResponceBO auth(LoginRequestBO loginRequestBO) {
        Response<LoginResponceBO> response;
        long startTime = ECDateUtils.currentTimeMillis();
        try {
            Call<LoginResponceBO> request = cloudCACommunicate.login(loginRequestBO);
            response = request.execute();
            log.info("Response {}", response);
            if (response.isSuccessful()) {
                LoginResponceBO dto = response.body();
                if (null == dto) {
                    log.error("Process check authentication Cloud CA. Response body null");
                    throw new ECBusinessException(MessageUtils.LOGIN);
                }
                log.info("Process check Authentication Cloud CA result {}", dto);
                return dto;
            } else {
                String errBody = response.errorBody() == null ? "" : response.errorBody().string();
                Gson g = new Gson();
                ResponceBO s = g.fromJson(errBody, ResponceBO.class);
                log.error("Response : {}", errBody);
                throw new ECBusinessException(MessageUtils.MYSIGN + s.getError());
            }
        } catch (IOException ex) {
            log.info("Error {}", new Date(startTime));
            throw new ECBusinessException(MessageUtils.LOGIN);
        } finally {
            log.info("Authentication in {}ms", (ECDateUtils.currentTimeMillis() - startTime));
        }
    }

    public CredentialsListResponceBO getListCredentials(String token, CredentialsListRequestBO credentialsListRequestBO) {
        Response<CredentialsListResponceBO> response;
        long startTime = ECDateUtils.currentTimeMillis();
        try {
            String bearerToken = "Bearer " + token;
            Call<CredentialsListResponceBO> request = cloudCACommunicate.getListCredentials(bearerToken, credentialsListRequestBO);
            response = request.execute();
            log.info("Response {}", response);
            if (response.isSuccessful()) {
                CredentialsListResponceBO dto = response.body();
                if (null == dto) {
                    log.error("Process get list credentials Cloud CA. Response body null");
                    throw new ECBusinessException(MessageUtils.GET_LIST_CREDENTIALS);
                }
                log.info("Process get list credentials Cloud CA result {}", dto);
                return dto;
            } else {
                String errBody = response.errorBody() == null ? "" : response.errorBody().string();
                Gson g = new Gson();
                ResponceBO s = g.fromJson(errBody, ResponceBO.class);
                log.error("Response : {}", errBody);
                throw new ECBusinessException(MessageUtils.MYSIGN + s.getError());
            }
        } catch (IOException ex) {
            log.info("Error {}", new Date(startTime));
            throw new ECBusinessException(MessageUtils.GET_LIST_CREDENTIALS);
        } finally {
            log.info("Get list credentials in {}ms", (ECDateUtils.currentTimeMillis() - startTime));
        }
    }

    public CredentialsInfoResponceBO getCredentialsInfo(String token, CredentialsInfoRequestBO credentialsInfoRequestBO) {
        Response<CredentialsInfoResponceBO> response;
        long startTime = ECDateUtils.currentTimeMillis();
        try {
            String bearerToken = "Bearer " + token;
            Call<CredentialsInfoResponceBO> request = cloudCACommunicate.getCredentialsInfo(bearerToken, credentialsInfoRequestBO);
            response = request.execute();
            log.info("Response {}", response);
            if (response.isSuccessful()) {
                CredentialsInfoResponceBO dto = response.body();
                if (null == dto) {
                    log.error("Process get info credentials Cloud CA. Response body null");
                    throw new ECBusinessException(MessageUtils.GET_INFO_CREDENTIALS);
                }
                log.info("Process get info credentials Cloud CA result {}", dto);
                return dto;
            } else {
                String errBody = response.errorBody() == null ? "" : response.errorBody().string();
                Gson g = new Gson();
                ResponceBO s = g.fromJson(errBody, ResponceBO.class);
                log.error("Response : {}", errBody);
                throw new ECBusinessException(MessageUtils.MYSIGN + s.getError());
            }
        } catch (IOException ex) {
            log.info("Error {}", new Date(startTime));
            throw new ECBusinessException(MessageUtils.GET_INFO_CREDENTIALS);
        } finally {
            log.info("Get info credentials in {}ms", (ECDateUtils.currentTimeMillis() - startTime));
        }
    }

    public CredentialsAuthorizeResponceBO credentialsAuthorize(String token, CredentialsAuthorizeRequestBO credentialsAuthorizeRequestBO) {
        Response<CredentialsAuthorizeResponceBO> response;
        long startTime = ECDateUtils.currentTimeMillis();
        try {
            String bearerToken = "Bearer " + token;
            Call<CredentialsAuthorizeResponceBO> request = cloudCACommunicate.credentialsAuthorize(bearerToken, credentialsAuthorizeRequestBO);
            response = request.execute();
            log.info("Response {}", response);
            if (response.isSuccessful()) {
                CredentialsAuthorizeResponceBO dto = response.body();
                if (null == dto) {
                    log.error("Process credentials authorize Cloud CA. Response body null");
                    throw new ECBusinessException(MessageUtils.CREDENTIALS_AUTHORIZE);
                }
                log.info("Process credentials authorize Cloud CA result {}", dto);
                return dto;
            } else {
                String errBody = response.errorBody() == null ? "" : response.errorBody().string();
                Gson g = new Gson();
                ResponceBO s = g.fromJson(errBody, ResponceBO.class);
                log.error("Response : {}", errBody);
                throw new ECBusinessException(MessageUtils.MYSIGN + s.getError());
            }
        } catch (IOException ex) {
            log.info("Error {}", new Date(startTime));
            throw new ECBusinessException(MessageUtils.CREDENTIALS_AUTHORIZE);
        } finally {
            log.info("Credentials authorize in {}ms", (ECDateUtils.currentTimeMillis() - startTime));
        }
    }

    public SignHashResponceBO signHash(String token, SignHashRequestBO signHashRequestBO) {
        Response<SignHashResponceBO> response;
        long startTime = ECDateUtils.currentTimeMillis();
        try {
            String bearerToken = "Bearer " + token;
            Call<SignHashResponceBO> request = cloudCACommunicate.signHash(bearerToken, signHashRequestBO);
            response = request.execute();
            log.info("Response {}", response);
            if (response.isSuccessful()) {
                SignHashResponceBO dto = response.body();
                if (null == dto) {
                    log.error("Process sign hash Cloud CA. Response body null");
                    throw new ECBusinessException(MessageUtils.SIGN_HASH);
                }
                log.info("Process sign hash Cloud CA result {}", dto);
                return dto;
            } else {
                String errBody = response.errorBody() == null ? "" : response.errorBody().string();
                Gson g = new Gson();
                ResponceBO s = g.fromJson(errBody, ResponceBO.class);
                log.error("Response : {}", errBody);
                throw new ECBusinessException(MessageUtils.MYSIGN + s.getError());
            }
        } catch (IOException ex) {
            log.info("Error {}", new Date(startTime));
            throw new ECBusinessException(MessageUtils.SIGN_HASH);
        } finally {
            log.info("Sign hash in {}ms", (ECDateUtils.currentTimeMillis() - startTime));
        }
    }
}
