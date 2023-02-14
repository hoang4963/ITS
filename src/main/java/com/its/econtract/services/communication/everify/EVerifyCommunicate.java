package com.its.econtract.services.communication.everify;

import com.its.econtract.controllers.request.ECVerifyRequest;
import com.its.econtract.controllers.response.ECTrucResponse;
import com.its.econtract.controllers.response.ECVerifyResponse;
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface EVerifyCommunicate {

    @POST("/")
    Call<ECVerifyResponse> eVerifySystemAction(
            @Header("X-API-KEY") String apiKey,
            @Header("ServiceType") String serviceType,
            @Body ECVerifyRequest request);


    // Fake API
    @Multipart
    @POST("/api/gateway/verify")
    Call<ECTrucResponse> eVerifyUpload(@Part MultipartBody.Part file);

    @GET("/api/gateway/history/{transactionId}/{id}")
    Call<Object> getDocumentHistory(@Path("transactionId") String transactionId, @Path("id") int id);

}
