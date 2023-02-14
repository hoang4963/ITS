/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.its.econtract.signature.cloudca.ca;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
public class CredentialsInfoRequestBO {
    @JsonProperty(value = "credentialID")
    private String credentialID;
    @JsonProperty(value = "certificates")
    private String certificates = "chain";
    @JsonProperty(value = "certInfo")
    private boolean certInfo = true;
    @JsonProperty(value = "authInfo")
    private boolean authInfo = true;

    public CredentialsInfoRequestBO(String credentialID) {
        this.credentialID = credentialID;
    }
}
