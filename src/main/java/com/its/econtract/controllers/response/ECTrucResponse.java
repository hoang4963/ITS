package com.its.econtract.controllers.response;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class ECTrucResponse {

    @JsonProperty(value = "transactionId")
    private String transactionId;

    @JsonProperty(value = "status")
    private boolean status;
}
