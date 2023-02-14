/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.its.econtract.services.communication.vvn;

import com.its.econtract.controllers.response.KYCResponse;
import com.its.econtract.controllers.response.OCRResponse;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

/**
 *
 * @author quangdt
 */
public interface VVNCommunicate{

    //** Form data request*/
    @Multipart
    @POST("ocr/recognition")
    public Call<OCRResponse> ocrRegconition(@Header("key") String apiKey,
                                            @Part("request_id") RequestBody requestId,
                                            @Part MultipartBody.Part file);

    @Multipart
    @POST("faceid/verification")
    public Call<KYCResponse> faceVerify(@Header("key") String apiKey,
                                        @Part("request_id") RequestBody requestId,
                                        @Part("return_feature") RequestBody returnFeature,
                                        @Part MultipartBody.Part fileId, @Part MultipartBody.Part fileFace);


}
