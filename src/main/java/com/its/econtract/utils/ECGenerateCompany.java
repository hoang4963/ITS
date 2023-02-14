package com.its.econtract.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ECGenerateCompany {
    public static final byte CMP = 1;
    public static final byte DOC = 2;

    public static int getCustomerSequence(Integer companyId) {
        if (companyId == null)
            return 0;
        return getCustomerSequence(companyId.intValue());
    }

    public static int getCustomerSequence(int companyId) {
        return companyId & 0x00FFFFFF;
    }

    public static int toCompanyId(byte companyId, int sequence) {
        return ((companyId << 24) & 0xFF000000) | (sequence & 0x00FFFFFF);
    }

    public static String generatePath(String pathGenerate, int companyId, int documentId) throws IOException {
        //CompanyId will be encrypted before using
        String relatedPath = String.format("%d/%d", companyId, documentId);
        String path = String.format("%s/%s", pathGenerate, relatedPath);
        Path upload = Paths.get(path).toAbsolutePath().normalize();
        Files.createDirectories(upload);
        return relatedPath;
    }
}
