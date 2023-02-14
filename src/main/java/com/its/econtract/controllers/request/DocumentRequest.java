package com.its.econtract.controllers.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Setter
@Getter
@Data
public class DocumentRequest {
    @ApiModelProperty(name = "document_id", example = "1", notes = "1: It is the contract id", required = true)
    @JsonProperty(value = "document_id")
    @NotNull(message = "Document Id must be required")
    private Integer documentId;

    @JsonProperty(value = "password")
    @ApiModelProperty(name = "password", example = "Its2022", notes = "The password needs to setup for pdf", required = false)
    private String password = "";
}
