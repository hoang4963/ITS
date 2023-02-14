package com.its.econtract.vtp.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class CheckImageResponse {
    @JsonProperty(value = "code")
    private Integer code;
    @JsonProperty(value = "message")
    private String message;
}
