package com.its.econtract.controllers.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Setter
@Getter
public class HashDocumentRequest {
    @JsonProperty(value = "document_id")
    @ApiModelProperty(name = "document_id", example = "1", notes = "1: It is the contract id", required = true)
    @NotNull(message = "Document Id is not empty")
    private Integer documentId;

    @ApiModelProperty(name = "assign_id", example = "1", notes = "1: (select assignee id)")
    @JsonProperty(value = "assign_id")
    private int assignId;

    private String pubCa = "";
}
