package com.its.econtract.signature.cloudca.communicate;

import com.its.econtract.signature.cloudca.ca.CredentialsAuthorizeRequestBO;
import com.its.econtract.signature.cloudca.ca.CredentialsAuthorizeResponceBO;
import com.its.econtract.signature.cloudca.ca.CredentialsInfoRequestBO;
import com.its.econtract.signature.cloudca.ca.CredentialsInfoResponceBO;
import com.its.econtract.signature.cloudca.ca.CredentialsListRequestBO;
import com.its.econtract.signature.cloudca.ca.CredentialsListResponceBO;
import com.its.econtract.signature.cloudca.ca.LoginRequestBO;
import com.its.econtract.signature.cloudca.ca.LoginResponceBO;
import com.its.econtract.signature.cloudca.ca.SignHashRequestBO;
import com.its.econtract.signature.cloudca.ca.SignHashResponceBO;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface CloudCACommunicate {

    @POST("/adss/service/ras/v1/login")
    Call<LoginResponceBO> login(@Body LoginRequestBO loginRequestBO);

    @POST("/adss/service/ras/csc/v1/credentials/list")
    Call<CredentialsListResponceBO> getListCredentials(@Header("Authorization") String token,
                                                       @Body CredentialsListRequestBO credentialsListRequestBO);

    @POST("/adss/service/ras/csc/v1/credentials/info")
    Call<CredentialsInfoResponceBO> getCredentialsInfo(@Header("Authorization") String token,
                                                       @Body CredentialsInfoRequestBO credentialsInfoRequestBO);

    @POST("/adss/service/ras/csc/v1/credentials/authorize")
    Call<CredentialsAuthorizeResponceBO> credentialsAuthorize(@Header("Authorization") String token,
                                                              @Body CredentialsAuthorizeRequestBO credentialsAuthorizeRequestBO);

    @POST("/adss/service/ras/csc/v1/signatures/signHash")
    Call<SignHashResponceBO> signHash(@Header("Authorization") String token,
                                      @Body SignHashRequestBO signHashRequestBO);
}
