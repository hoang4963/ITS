package com.its.econtract.utils;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import org.apache.logging.log4j.util.Strings;

import java.io.IOException;
import java.text.Normalizer;
import java.util.Base64;
import java.util.Random;
import java.util.regex.Pattern;

public class StringUtil {
    static String strDigits = "0123456789abcdefghijklmnpqrstuvwxyzABCDEFGHIJKLMNPQRSTUVWXYZ";

    public static String randomString(int size) {
        return randomString(strDigits, size);
    }

    public static String removeSpecCharacter(String input) {
        if (Strings.isEmpty(input)) return "";
        String temp = Normalizer.normalize(input, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(temp).replaceAll("");
    }

    public static String randomString(String digits, int size) {
        StringBuilder passBuilder = new StringBuilder();
        passBuilder.setLength(0);
        int tableSize = digits.length();
        Random random = new Random();

        for (int i = 0; i < size; ++i) {
            passBuilder.append(digits.charAt(random.nextInt(tableSize)));
        }

        return passBuilder.toString();
    }

    public static String toNonUnicode(String str) {
        str = str.replace("Đ", "D");
        str = str.replace("đ", "d");
        String normalized = Normalizer.normalize(str, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
        return normalized;
    }

    public static boolean isEmptyOrBlank(String value) {
        if (value == null || "".equals(value.trim())) {
            return true;
        }
        return false;
    }

    public static float scaleRate(float scale, float scaleSize) {
        return scaleSize / scale;
    }

    public static String buildDesResource(String path, int documentId, int cmpId) {
        try {
            String name = String.valueOf(ECGenerateCompany.toCompanyId(ECGenerateCompany.DOC, documentId));
            int companyId = ECGenerateCompany.toCompanyId(ECGenerateCompany.CMP, cmpId);
            String relatedPath = String.format("%s/%s-%d-signed.pdf", ECGenerateCompany.generatePath(path, companyId, documentId), name, ECDateUtils.currentTimeMillis());
            return relatedPath;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static ImageData buildImageData(String imageSrc) {
        String[] img = imageSrc.split(",");
        String[] type = img[0].split("/");
        switch (type[1].split(";")[0]) {
            case "jpeg":
                return ImageDataFactory.createJpeg(Base64.getDecoder().decode(img[1].getBytes()));
            default:
                return ImageDataFactory.createPng(Base64.getDecoder().decode(img[1].getBytes()));
        }
    }
}
