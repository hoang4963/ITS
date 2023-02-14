package com.its.econtract.controllers.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotificationCompleteRequest {
    @JsonProperty(value = "document_id")
    @ApiModelProperty(name = "document_id", example = "1", notes = "1: It is the contract id", required = true)
    private int documentId;
}
