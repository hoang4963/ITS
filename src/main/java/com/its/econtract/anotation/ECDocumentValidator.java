package com.its.econtract.anotation;

import org.apache.commons.io.FilenameUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.List;

public class ECDocumentValidator implements ConstraintValidator<ECDocumentAllow.FileValidator, MultipartFile> {

    private boolean required;

    private List<String> documentTypes;

    private List<String> contentTypes;

    private String message;

    private boolean checkContentType;

    @Override
    public void initialize(ECDocumentAllow.FileValidator constraintAnnotation) {
        this.message = constraintAnnotation.message();
        this.required = constraintAnnotation.required();
        this.documentTypes = Arrays.asList(constraintAnnotation.types());
        this.checkContentType = constraintAnnotation.checkContentType();
        this.contentTypes = Arrays.asList(constraintAnnotation.contentTypes());
    }

    @Override
    public boolean isValid(MultipartFile multipartFile, ConstraintValidatorContext constraintValidatorContext) {
        boolean result = true;
        if (multipartFile == null && required) {
            this.message = "File upload is required";
            result = false;
        } else {
            if (multipartFile == null) return true;
            String fileType = FilenameUtils.getExtension(multipartFile.getOriginalFilename());
            if (!this.documentTypes.contains(fileType)) {
                this.message = "File type does not support";
                result = false;
            }
            String contentType = multipartFile.getContentType();
            if (!isSupportedContentType(contentType)) {
                this.message = "File type does not support";
                result = false;
            }
        }
        if (!result) {
            constraintValidatorContext.disableDefaultConstraintViolation();
            constraintValidatorContext.buildConstraintViolationWithTemplate(this.message).addConstraintViolation();
        }
        return result;
    }


    private boolean isSupportedContentType(String contentType) {
        if (checkContentType){
            return this.contentTypes.contains(contentType);
        }else {
            return true;
        }
    }
}
