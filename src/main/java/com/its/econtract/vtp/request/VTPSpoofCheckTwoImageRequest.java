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
public class VTPSpoofCheckTwoImageRequest {
    @JsonProperty(value = "client_code")
    private String clientCode;
    @JsonProperty(value = "image")
    private String imageFront;
    @JsonProperty(value = "image_other")
    private String imageBack;
}
