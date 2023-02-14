package com.its.econtract.entity.converter;

import com.google.gson.Gson;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Arrays;

@Log4j2
public class Aes {

    public static String encrypt(byte[] keyValue, String plaintext) throws Exception {
        Key key = new SecretKeySpec(keyValue, "AES");
        //serialize
        String serializedPlaintext = plaintext;
        byte[] plaintextBytes = serializedPlaintext.getBytes("UTF-8");

        Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
        c.init(Cipher.ENCRYPT_MODE, key);
        byte[] iv = c.getIV();
        System.out.println(Base64.encodeBase64String(iv));
        byte[] encVal = c.doFinal(plaintextBytes);
        String encryptedData = Base64.encodeBase64String(encVal);
        System.out.println(encryptedData);
        SecretKeySpec macKey = new SecretKeySpec(keyValue, "AES");
        Mac hmacSha256 = Mac.getInstance("HmacSHA256");
        hmacSha256.init(macKey);
        hmacSha256.update(Base64.encodeBase64(iv));
        byte[] calcMac = hmacSha256.doFinal(encryptedData.getBytes("UTF-8"));
        String mac = new String(Hex.encodeHex(calcMac));
        System.out.println(mac);
        AesEncryptionData aesData = new AesEncryptionData(
                Base64.encodeBase64String(iv),
                encryptedData,
                mac);

        String aesDataJson = new Gson().toJson(aesData);

        return Base64.encodeBase64String(aesDataJson.getBytes("UTF-8"));
    }

    public static String decrypt(byte[] keyValue, String ivValue, String encryptedData, String macValue) throws Exception {
        Key key = new SecretKeySpec(keyValue, "AES");
        byte[] iv = Base64.decodeBase64(ivValue.getBytes("UTF-8"));
        byte[] decodedValue = Base64.decodeBase64(encryptedData.getBytes("UTF-8"));

        SecretKeySpec macKey = new SecretKeySpec(keyValue, "AES");
        Mac hmacSha256 = Mac.getInstance("HmacSHA256");
        hmacSha256.init(macKey);
        hmacSha256.update(ivValue.getBytes("UTF-8"));
        byte[] calcMac = hmacSha256.doFinal(encryptedData.getBytes("UTF-8"));
        byte[] mac = Hex.decodeHex(macValue.toCharArray());
        if (!Arrays.equals(calcMac, mac))
            return "MAC mismatch";

        Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding"); // or PKCS5Padding
        c.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
        byte[] decValue = c.doFinal(decodedValue);

        int firstQuoteIndex = 0;
        return new String(Arrays.copyOfRange(decValue, firstQuoteIndex, decValue.length));
    }
}
