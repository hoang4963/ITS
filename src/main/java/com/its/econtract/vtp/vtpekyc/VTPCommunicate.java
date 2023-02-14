package com.its.econtract.vtp.vtpekyc;

import com.its.econtract.vtp.request.*;
import com.its.econtract.vtp.response.AuthenticationResponse;
import com.its.econtract.vtp.response.BaseResponse;
import com.its.econtract.vtp.response.VTPKycResponse;
import com.its.econtract.vtp.response.VTPOCRResponse;
import retrofit2.Call;
import retrofit2.http.*;

public interface VTPCommunicate {

    @POST("/ekyc/user/authent")
    public Call<AuthenticationResponse> authentication(@Body VTPAuthenticationRequest authenticationRequest);


    @POST("/ekyc/v1/face_matching")
    public Call<VTPKycResponse> verify(@Header("token") String token,
                                       @Body VTPKycRequest kycRequest);

    @POST("/ekyc/v1/idcard_quality")
    public Call<BaseResponse> qualityCheck(@Header("token") String token,
                                           @Body VTPIdentificationCheckRequest identificationCheckRequest);

    @POST("/ekyc/v1/idcard_pack_spoof")
    public Call<BaseResponse> spoofCheckTwoImage(@Header("token") String token,
                                           @Body VTPSpoofCheckTwoImageRequest vtpSpoofCheckTwoImageRequest);

    @POST("/ekyc/v1/idcard_spoof")
    public Call<BaseResponse> spoofCheck(@Header("token") String token,
                                         @Body VTPIdentificationCheckRequest identificationCheckRequest);

    @POST("/ekyc/v1/face_quality")
    public Call<BaseResponse> faceQualityCheck(@Header("token") String token,
                                               @Body VTPIdentificationCheckRequest identificationCheckRequest);

    @POST("/ekyc/v1/face_liveness")
    public Call<VTPKycResponse> faceLiveness(@Header("token") String token,
                                             @Body VTPIdentificationCheckRequest identificationCheckRequest);
    @POST("/ekyc/v1/idcard_ocr")
    public Call<VTPOCRResponse> ocr(@Header("token") String token,
                                    @Body VTPOCRRequest vtpocrRequest);

}
