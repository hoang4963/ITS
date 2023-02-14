package com.its.econtract.signature.cloudca.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SignHashRequest {
    @JsonProperty(value = "user_id")
    private String userID;

    @JsonProperty(value = "credential_id")
    private String credentialID;
}
