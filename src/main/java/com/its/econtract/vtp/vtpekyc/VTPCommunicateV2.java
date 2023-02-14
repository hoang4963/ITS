package com.its.econtract.vtp.vtpekyc;

import com.its.econtract.controllers.response.KYCResponse;
import com.its.econtract.controllers.response.OCRResponse;
import com.its.econtract.vtp.request.KYCRequestV2;
import com.its.econtract.vtp.request.OCRRequestV2;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface VTPCommunicateV2 {
    @POST("/v3.2/ocr/recognition")
    public Call<OCRResponse> ocr(@Header("key") String key,
                                 @Body OCRRequestV2 ocrRequest);

    @POST("/v3.2/faceid/verification")
    public Call<KYCResponse> verify(@Header("key") String key,
                                    @Body KYCRequestV2 kycRequest);
}
