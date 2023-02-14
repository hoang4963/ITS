package com.its.econtract.vtp.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import javax.validation.constraints.NotNull;


@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ECOCRRequest {

    @NotNull(message = "Customer ID is not empty")
    @JsonProperty(value = "customer_id")
    private Integer customerId;

    @NotNull(message = "Company ID is not empty")
    @JsonProperty(value = "company_id")
    private Integer companyId;
}
