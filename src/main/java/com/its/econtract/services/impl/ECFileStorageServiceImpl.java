package com.its.econtract.services.impl;

import com.its.econtract.services.IECFileStorageService;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.util.Strings;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

@Log4j2
@Component
public class ECFileStorageServiceImpl implements IECFileStorageService {
    @Value(value = "${file.upload-dir}")
    private String path;

    @Override
    public void init() {
        try {
            Path root = Paths.get(path);
            Files.createDirectory(root);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize folder for upload!");
        }
    }

    @Override
    public void save(MultipartFile file) {
        //ToDo
    }

    @Override
    public Resource load(String filename) {
        return null;
    }

    @Override
    public void deleteAll() {

    }

    @Override
    public Stream<Path> loadAll() {
        return null;
    }

    @Override
    public String mergeFile(List<String> files,
                            String password,
                            String file_name) throws Exception {
        if (CollectionUtils.isEmpty(files)) return "";
        //Instantiating PDFMergerUtility class
        PDFMergerUtility merger = new PDFMergerUtility();
        for (String file: files) {
            merger.addSource(file);
        }
        //Setting the destination file

        File output = new File(file_name);
        merger.setDestinationStream(FileUtils.openOutputStream(output));
        merger.mergeDocuments(MemoryUsageSetting.setupTempFileOnly());
        if (Strings.isNotBlank(password)) encryptData(output, password);
        return file_name;
    }

    private void encryptData(File output, String password) throws Exception {
        log.info("BEGIN encryptData");
        PDDocument document = PDDocument.load(output);
        try {
            AccessPermission ap = new AccessPermission();
            StandardProtectionPolicy stpp = new StandardProtectionPolicy(password, password, ap);
            // step 4. Setting the length of Encryption key
            stpp.setEncryptionKeyLength(128);
            // step 5. Setting the permission
            stpp.setPermissions(ap);
            // step 6. Protecting the PDF file
            document.protect(stpp);
        } finally {
            document.close();
            log.info("END encryptData");
        }
    }
}
