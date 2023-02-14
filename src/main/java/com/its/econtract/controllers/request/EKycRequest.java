package com.its.econtract.controllers.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class EKycRequest {
    @NotNull(message = "Assignee Id is not empty")
    @JsonProperty(value = "assignee_id")
    private int assigneeId;

    @JsonProperty(value = "type")
    private byte type;
}
