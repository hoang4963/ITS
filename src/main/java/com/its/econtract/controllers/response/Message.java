/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.its.econtract.controllers.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 *
 * @author quangdt
 */
@Getter
@Setter
@ToString
public class Message{
    @JsonProperty("api_version")
    String apiVersion;
    @JsonProperty("copy_right") String copyRight;
    @JsonProperty("error_code")
    String errorCode;
    @JsonProperty("error_message")
    String errorMessage;

}
