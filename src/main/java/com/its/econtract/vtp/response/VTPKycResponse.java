package com.its.econtract.vtp.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class VTPKycResponse extends BaseResponse{
    @JsonProperty(value = "verify_result")
    private Boolean verifyResult;
    @JsonProperty(value = "score")
    private Double score;

    public VTPKycResponse(BaseResponse baseResponse, Boolean verifyResult, Double score) {
        this.verifyResult = verifyResult;
        this.score = score;
        super.setCode(baseResponse.getCode());
        super.setMessage(baseResponse.getMessage());
        super.setRequestId(baseResponse.getRequestId());
        super.setRequestTime(baseResponse.getRequestTime());
        super.setResponseTime(baseResponse.getResponseTime());
        super.setSignature(baseResponse.getSignature());
    }
}
