package com.its.econtract.anotation;


import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

public interface ECDocumentAllow {

    @Documented
    @Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
    @Constraint(
            validatedBy = {ECDocumentValidator.class}
    )
    @Retention(RetentionPolicy.RUNTIME)
    @interface FileValidator {
        String message() default "Invalid document type";

        String[] types() default {"doc", "docx", "pdf"};

        String[] contentTypes() default {"application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                                        "application/pdf",
                                        "application/msword"};

        boolean required() default false;

        boolean checkContentType() default true;

        Class<?>[] groups() default {};

        Class<? extends Payload>[] payload() default {};
    }


    @Documented
    @Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
    @Constraint(
            validatedBy = {ECPhoneValidator.class}
    )
    @Retention(RetentionPolicy.RUNTIME)
    @interface PhoneValidator {
        String message() default "Invalid document type";

        boolean required() default false;

        Class<?>[] groups() default {};

        Class<? extends Payload>[] payload() default {};
    }
}
