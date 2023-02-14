package com.its.econtract.controllers.response;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class ECVerifyResponse {

    @JsonProperty(value = "content")
    private ECVerifyContent content;

    @JsonProperty(value = "info")
    private ECVerifyInfo info;

    @Setter
    @Getter
    public static class ECVerifyContent {
        private Object data;
        @JsonProperty(value = "transactionId")
        private Object transactionId;
    }

    @Setter
    @Getter
    public static class ECVerifyInfo {
        private String version;
        private String senderId;
        private String receiverId;
        private int messageType;
        private long sendDate;
        private String messageId;
        private String referenceMessageId;
        private int responseCode;
        private String responseMessage;
    }

    @Setter
    @Getter
    public static class EVerifyTimeToken {
        private String timestampToken;
        private String timestamp;
        private String algorithm;
    }
}
