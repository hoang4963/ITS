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
public class KYCRequestV2 {

    @JsonProperty(value = "image_card")
    private String imageCard;

    @JsonProperty(value = "image_live")
    private String imageLive;

    @JsonProperty(value = "request_id")
    private String requestId;
}
