package com.its.econtract.vtp.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class VTPAuthenticationRequest {
    @JsonProperty(value = "username")
    private String username;
    @JsonProperty(value = "password")
    private String password;
}
