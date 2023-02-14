package com.its.econtract.controllers.request;


import com.its.econtract.anotation.ECDocumentAllow;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Data
public class MobileRequest {
    @ECDocumentAllow.PhoneValidator(message = "Phone is required")
    private String message;
}
