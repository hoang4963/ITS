package com.its.econtract.utils;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;

import java.util.ArrayList;
import java.util.List;

@Component
public class MessageUtils {

    @Autowired
    @Qualifier("messageSource")
    private MessageSource messageSource;

    public String getMessage(String key) {
        return this.messageSource.getMessage(key, new String[]{""}, LocaleContextHolder.getLocale());
    }

    public String getMessage(String key, Object... params) {
        List<String> paramStrs = new ArrayList();
        Object[] inputs = params;
        int length = params.length;

        for(int i = 0; i < length; ++i) {
            Object param = inputs[i];
            paramStrs.add(String.valueOf(param));
        }

        return this.messageSource.getMessage(key, paramStrs.toArray(), LocaleContextHolder.getLocale());
    }

    public String getMessage(FieldError field) {
        return this.messageSource.getMessage(field, LocaleContextHolder.getLocale());
    }

    public static final String SYS_ERR = "SYS_ERR";
    public static final String NA= "N/A";
    public static final String ERR_005 = "ERR_005";
    public static final String FAKE = "FAKE";
    public static final String ERROR_FAKE = "Khuôn mặt không hợp lệ vui lòng chụp lại";
    public static final String AUTH = "Authentication fail";
    public static final String FACE_MATCH = "Face match fail";
    public static final String QUALITY_CHECK = "Quality check fail";
    public static final String SPOOF_CHECK = "Spoof check fail";
    public static final String FACE_QUALITY_CHECK = "Face quality fail";
    public static final String FACE_LIVENESS = "Face liveness fail";
    public static final String OCR = "Ocr fail";
    public static final String  SPOOF_CHECK_TWO_IMAGE = "Spoof check two image fail";
    public static final String NOT_FOUND_INFO= "NOT_FOUND_INFO";
    // Log cloud CA
    public static final String LOGIN = "JAVA_401";
    public static final String GET_LIST_CREDENTIALS = "JAVA_601";
    public static final String GET_INFO_CREDENTIALS = "JAVA_602";
    public static final String CREDENTIALS_AUTHORIZE = "JAVA_603";
    public static final String SIGN_HASH = "JAVA_604";
    public static final String MYSIGN = "MS_";
    public static final String CHECK_CCCD = "JAVA_6021";
    public static final String CREATE_HASH = "JAVA_6041";
    public static final String GET_SAD = "JAVA_6042";
}
