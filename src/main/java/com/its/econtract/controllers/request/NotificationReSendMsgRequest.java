package com.its.econtract.controllers.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotificationReSendMsgRequest {
    @JsonProperty(value = "conversationId")
    private Integer conversationId;
}
