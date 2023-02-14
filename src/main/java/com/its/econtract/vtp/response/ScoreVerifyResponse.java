package com.its.econtract.vtp.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ScoreVerifyResponse {
    @JsonProperty(value = "score")
    private Double score;
    @JsonProperty(value = "verify_result")
    private Boolean verifyResult;
}
