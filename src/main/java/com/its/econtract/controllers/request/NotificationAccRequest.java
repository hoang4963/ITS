package com.its.econtract.controllers.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Maps;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Setter
@Getter
public class NotificationAccRequest {
    @JsonProperty(value = "template_name")
    private String templateName;

    @JsonProperty(value = "exts")
    private Map<String, Object> exts = Maps.newConcurrentMap();

    @ApiModelProperty(name = "type", example = "1")
    @JsonProperty(value = "type")
    private int type = -1;

    @JsonProperty(value = "email")
    private String email;

    @JsonProperty(value = "number_phone")
    private String numberPhone;

}
