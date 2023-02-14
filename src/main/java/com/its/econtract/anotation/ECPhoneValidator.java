package com.its.econtract.anotation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ECPhoneValidator implements ConstraintValidator<ECDocumentAllow.PhoneValidator, String> {

    private String message;

    private boolean required;

    @Override
    public void initialize(ECDocumentAllow.PhoneValidator constraintAnnotation) {
        this.message = constraintAnnotation.message();
        this.required = constraintAnnotation.required();
    }

    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        return false;
    }
}
