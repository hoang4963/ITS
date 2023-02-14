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
@AllArgsConstructor
@NoArgsConstructor
public class CredentialsInfoResponceBO extends ResponceBO {

    @JsonProperty(value = "description")
    private String description;

    @JsonProperty(value = "key")
    private KeyBO key;

    @JsonProperty(value = "cert")
    private CertBO cert;

    @JsonProperty(value = "PIN")
    private PINBO PIN;

    @JsonProperty(value = "OTP")
    private OTPBO OTP;

    @JsonProperty(value = "authMode")
    private String authMode;

    @JsonProperty(value = "SCAL")
    private String SCAL;

    @JsonProperty(value = "multisign")
    private int multisign;

    @JsonProperty(value = "lang")
    private String lang;

}
