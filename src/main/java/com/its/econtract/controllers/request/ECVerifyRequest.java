package com.its.econtract.controllers.request;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.its.econtract.utils.ECDateUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ECVerifyRequest {

    private ECVerifyContent content;

    private ECVerifyInfo info;

    private String signature;

    @Setter
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ECVerifyContent {
        private Map<String, Object> data;
        private String transactionId;

        public ECVerifyContent(Map<String, Object> data) {
            this.data = data;
        }
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ECVerifyInfo {
        private String version;
        private String senderId;
        private String receiverId;
        private int messageType;
        private long sendDate;
        private String messageId;
    }

    @Setter
    @Getter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ECVerifySignature {
        private String digest;
        private String signature;
        private String hashAlgorithm  = "SHA256";
        private String signatureAlgorithm  = "RSA";
        private long timestamp  = ECDateUtils.currentTimeMillis();
        private String x509Certificate;
        private String identityId;
        private int signatureGroup;
        private Byte signatureType;
    }
}
