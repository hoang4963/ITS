package com.its.econtract.controllers.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TextDocumentRequest {
    @JsonProperty(value = "document_id")
    @ApiModelProperty(name = "document_id", example = "1", notes = "1: It is the contract id", required = true)
    private int documentId;

    @ApiModelProperty(name = "assign_id", example = "1", notes = "1: (select assignee id)")
    @JsonProperty(value = "assign_id")
    private int assignId;

    @JsonProperty(value = "scale")
    private float scale = 1.5f;
}
