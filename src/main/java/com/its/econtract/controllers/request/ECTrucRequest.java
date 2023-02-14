package com.its.econtract.controllers.request;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.its.econtract.controllers.response.ECVerifyResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@ToString
public class ECTrucRequest {

    private ECTrucContentResponse content;

    private ECVerifyResponse.ECVerifyInfo info;

    private String signature;

    @Setter
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @ToString
    public static class ECTrucContentResponse {
        private String transactionId;
        private ECTrucDataResponse data = new ECTrucDataResponse();
    }

    @Setter
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @ToString
    public static class ECTrucDataResponse {
        private String signature;
        private String algorithm;
        private String digest;
        private String verificationCode;
        private String image;
    }
}
