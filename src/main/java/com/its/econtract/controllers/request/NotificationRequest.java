package com.its.econtract.controllers.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Maps;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Map;

@Setter
@Getter
public class NotificationRequest {

    @JsonProperty(value = "template_name")
    private String templateName;

    @JsonProperty(value = "document_id")
    @ApiModelProperty(name = "document_id", example = "1", notes = "1: It is the contract id", required = true)
    private int documentId;

    @JsonProperty(value = "assignee_ids")
    @ApiModelProperty(name = "assignee_ids", example = "[1,2,3]", required = true)
    @NotEmpty(message = "The assignee is required")
    private List<Integer> assignIds;

    @JsonProperty(value = "exts")
    private Map<String, Object> exts = Maps.newConcurrentMap();

    @ApiModelProperty(name = "type", example = "1")
    @JsonProperty(value = "type")
    private int type = -1;

    @ApiModelProperty(name = "notify_type", example = "1")
    @JsonProperty(value = "notify_type")
    private int notifyType = -1;
}
