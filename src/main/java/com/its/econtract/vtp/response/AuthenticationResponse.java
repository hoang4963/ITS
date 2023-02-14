package com.its.econtract.vtp.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class AuthenticationResponse extends BaseResponse {
    @JsonProperty(value = "token")
    private String token;
}
