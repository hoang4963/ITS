package com.its.econtract.vtp.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class OCRRequestV2 {

    @JsonProperty(value = "image")
    private String image;

    @JsonProperty(value = "request_id")
    private String requestId;
}
