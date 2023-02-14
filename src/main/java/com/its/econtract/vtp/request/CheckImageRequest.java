package com.its.econtract.vtp.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CheckImageRequest {

    @NotNull(message = "Image card is not empty")
    @JsonProperty(value = "image_card")
    private String idImage;

    @NotNull(message = "Customer ID is not empty")
    @JsonProperty(value = "customer_id")
    private Integer customerId;

    @NotNull(message = "Company ID is not empty")
    @JsonProperty(value = "company_id")
    private Integer companyId;
}
