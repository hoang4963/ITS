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
public class SampleRequest {
    @ApiModelProperty(name = "sample_id", example = "1", notes = "1: It is the contract id", required = true)
    @JsonProperty(value = "sample_id")
    @NotNull(message = "Sample Id must be required")
    private Integer sampleId;

    @JsonProperty(value = "password")
    @ApiModelProperty(name = "password", example = "Its2022", notes = "The password needs to setup for pdf", required = false)
    private String password = "";
}

