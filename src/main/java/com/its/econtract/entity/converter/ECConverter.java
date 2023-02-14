package com.its.econtract.entity.converter;

import com.google.gson.Gson;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.persistence.AttributeConverter;
import javax.persistence.Convert;

@Convert
@Component
public class ECConverter implements AttributeConverter<String, String> {

    private static final Gson gson = new Gson();
    @Value("${laravel_app_key}")
    private String laravelApp;

    @Override
    public String convertToDatabaseColumn(String s) {
        try {
            return Aes.encrypt(Base64.decodeBase64(laravelApp), s);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String convertToEntityAttribute(String encrypt) {
        String content = new String(Base64.decodeBase64(encrypt));
        AesEncryptionData encryptionData = gson.fromJson(content, AesEncryptionData.class);
        try {
            return Aes.decrypt(Base64.decodeBase64(laravelApp), encryptionData.iv, encryptionData.value, encryptionData.mac);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

}
