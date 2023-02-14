package com.its.econtract.vtp.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class BaseResponse {
    @JsonProperty(value = "code")
    private Integer code;
    @JsonProperty(value = "message")
    private String message;
    @JsonProperty(value = "request_time")
    private String requestTime;
    @JsonProperty(value = "response_time")
    private String responseTime;
    @JsonProperty(value = "request_id")
    private String requestId;
    @JsonProperty(value = "signature")
    private String signature;
}
