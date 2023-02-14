package com.its.econtract.facade;

import com.google.common.collect.Lists;
import com.its.econtract.controllers.request.SampleRequest;
import com.its.econtract.entity.*;
import com.its.econtract.entity.ECDocumentSampleResources;
import com.its.econtract.exceptions.ECBusinessException;
import com.its.econtract.utils.ECDateUtils;
import com.its.econtract.utils.ECGenerateCompany;
import com.its.econtract.controllers.request.DocumentRequest;
import com.its.econtract.repository.*;
import com.its.econtract.services.IECFileStorageService;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.util.Strings;
import org.jodconverter.core.DocumentConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Log4j2
@Component
public class ECConvertDocumentFacade extends ECBaseFacade {
    private String LOCAL = "local";

    @Value(value = "${file.upload-dir}")
    private String path;

    private Path root;

    @Value("#{'${document.convert_types}'.split(',')}")
    private List<String> docTypes;

    @Autowired
    private ECDocumentRepository documentRepository;

    @Autowired
    private ECDocumentResourcesRepository resourceRepository;

    @Autowired
    private ECDocumentSampleResourcesRepository sampleResourcesRepository;

    @Autowired
    private IECFileStorageService fileStorageService;

    @Autowired
    private ECDocumentResourceSignRepository signRepository;

    @Autowired
    private ECDocumentSampleRepository sampleRepository;

    @Autowired
    @Qualifier(value = "localDocumentConverter")
    private DocumentConverter documentConverter;

//    @Autowired
//    @Qualifier(value = "onlineDocumentConverter")
//    private org.jodconverter.DocumentConverter onlineConverter;

    @PostConstruct
    public void initOfficeManager() {
        System.out.println("initOfficeManager");
        this.root = Paths.get(path);
    }

    @Autowired
    private Environment environment;

    public CompletableFuture<String> convertPDF(MultipartFile file) throws Exception {
        String contentType = file.getContentType();
        String fileName = String.format("%s/%s.pdf", path, FilenameUtils.getBaseName(file.getOriginalFilename()));
        if (isPDF(contentType)) {
            Files.copy(file.getInputStream(), this.root.resolve(file.getOriginalFilename()));
        } else {
            List<String> profiles = Lists.newArrayList(this.environment.getActiveProfiles());
//            if (!profiles.contains(LOCAL)) {
//                log.info("BEGIN convert with docker");
//                log.info("[convertPDF] convert WORDs -> PDF with dev environment");
//                InputStream inputStream = file.getInputStream();
//                onlineConverter.convert(inputStream).as(DocumentFormat.builder().extension("PDF").build()).to(new File(fileName)).execute();
//            } else {
                log.info("[convertPDF] convert WORDs -> PDF with local environment");
                documentConverter.convert(file.getInputStream()).to(new File(fileName)).execute();
//            }
        }

        return CompletableFuture.completedFuture(fileName);
    }

    private String processConvert(String filePath) throws Exception {
        InputStream inputStream = null;
        try {
            File input = new File(filePath);
            inputStream = FileUtils.openInputStream(input);
            String fileName = String.format("%s/%s-%d.pdf", path, FilenameUtils.getBaseName(input.getName()), ECDateUtils.currentTimeMillis());
            List<String> profiles = Lists.newArrayList(this.environment.getActiveProfiles());
            log.info("processConvert: {}", profiles);
//            if (!profiles.contains(LOCAL)) {
//                log.info("BEGIN convert with docker");
//                log.info("[processConvert] convert WORDs -> PDF with dev environment");
//                onlineConverter.convert(input).as(DocumentFormat.builder().extension("PDF").build()).to(new File(fileName)).execute();
//            } else {
//                log.info("[processConvert] convert WORDs -> PDF with local environment");
//                documentConverter.convert(inputStream).to(new File(fileName)).execute();
//            }
            log.info("[convertPDF] convert WORDs -> PDF with local environment");
            documentConverter.convert(inputStream).to(new File(fileName)).execute();
            return fileName;
        } finally {
            if (inputStream != null) inputStream.close();
        }
    }

    @Async
    public CompletableFuture<String> convertDocumentById(DocumentRequest documentRequest) throws Exception {
        ECDocuments doc = fetchDocument(documentRequest.getDocumentId());

        List<ECDocumentResources> resources = resourceRepository.fetchDocumentResources(documentRequest.getDocumentId());
        if (CollectionUtils.isEmpty(resources)) {
            throw new ECBusinessException("Not found any resources of this document in the system");
        }

        List<String> rmFiles = Lists.newArrayList();
        try {
            List<String> fileMerge = Lists.newArrayList();
            for (ECDocumentResources r : resources) {
                if (Strings.isEmpty(r.getFilePathRaw())) throw new Exception("Not found file resource");
                String rawPath = String.format("%s/%s", path, r.getFilePathRaw());
                if (isNeedConvert(r.getFileTypeRaw())) {
                    String convertPath = processConvert(rawPath);
                    log.info("convertPDF path: {}", convertPath);
                    rmFiles.add(convertPath);
                    fileMerge.add(convertPath);
                } else {
                    fileMerge.add(rawPath);
                }
            }

            String newPath = this.mergeDocumentResources(fileMerge, documentRequest.getPassword(), doc);

            //Async update resource if need.
            return CompletableFuture.completedFuture(newPath);
        } finally {
            rmTempFiles(rmFiles);
        }
    }

    public CompletableFuture<String> convertPDFSample(MultipartFile file) throws Exception {
        String contentType = file.getContentType();
        String fileName = String.format("%s/%s.pdf", path, FilenameUtils.getBaseName(file.getOriginalFilename()));
        if (isPDF(contentType)) {
            Files.copy(file.getInputStream(), this.root.resolve(file.getOriginalFilename()));
        } else {
            List<String> profiles = Lists.newArrayList(this.environment.getActiveProfiles());
            log.info("[convertPDF] convert WORDs -> PDF with local environment");
            documentConverter.convert(file.getInputStream()).to(new File(fileName)).execute();
        }

        return CompletableFuture.completedFuture(fileName);
    }

    @Async
    public CompletableFuture<String> convertDocumentByIdSample(SampleRequest sampleRequest) throws Exception {
        ECDocumentSample sample = fetchDocumentSample(sampleRequest.getSampleId());

        List<ECDocumentSampleResources> resources = sampleResourcesRepository.fetchDocumentSampleResources(sampleRequest.getSampleId());

        if (CollectionUtils.isEmpty(resources)) {
            throw new ECBusinessException("Not found any resources of this document in the system");
        }

        List<String> rmFiles = Lists.newArrayList();
        try {
            List<String> fileMerge = Lists.newArrayList();
            for (ECDocumentSampleResources r : resources) {
                if (Strings.isEmpty(r.getFilePathRaw())) throw new Exception("Not found file resource");
                String rawPath = String.format("%s/%s", path, r.getFilePathRaw());
                if (isNeedConvert(r.getFileTypeRaw())) {
                    String convertPath = processConvert(rawPath);
                    log.info("convertPDF path: {}", convertPath);
                    rmFiles.add(convertPath);
                    fileMerge.add(convertPath);
                } else {
                    fileMerge.add(rawPath);
                }
            }

            String newPath = this.mergeDocumentResourcesSample(fileMerge, sampleRequest.getPassword(), sample);

            //Async update resource if need.
            return CompletableFuture.completedFuture(newPath);
        } finally {
            rmTempFiles(rmFiles);
        }
    }

    @Transactional
    protected String mergeDocumentResourcesSample(List<String> resourcePatch, String password, ECDocumentSample sample) throws Exception {
        Optional<ECDocumentSample> signOptional = sampleRepository.findById(sample.getId());
        if (!signOptional.isPresent()) {
            throw new ECBusinessException("Not found sample document in the system");
        }
        ECDocumentSample sign = signOptional.get();

        String fileMerged = String.valueOf(ECGenerateCompany.toCompanyId(ECGenerateCompany.DOC, sample.getId()));
        int companyId = ECGenerateCompany.toCompanyId(ECGenerateCompany.CMP, sample.getCompanyId());
        String generated = ECGenerateCompany.generatePath(path, companyId, sample.getId());
        String frm = "sample/" + String.format("%s/%s-%d.pdf", generated, fileMerged, ECDateUtils.currentTimeMillis());
        fileStorageService.mergeFile(resourcePatch, password, String.format("%s/%s", path, frm));
        sign.setDocPathRaw(frm);
        sign.setCreatedBy(sample.getCreatedBy()); //Only allow creator to request create documents & merge
        sampleRepository.save(sign);
        //Async update resource if need.
        return frm;
    }

    private ECDocumentSample fetchDocumentSample(int sampleId) {
        Optional<ECDocumentSample> sampleOptional = sampleRepository.findById(sampleId);
        if (!sampleOptional.isPresent()) {
            throw new ECBusinessException("This document is not found in the system", HttpStatus.NOT_FOUND);
        }
        if (sampleOptional.get().getDeleteFlag() == 1) {
            throw new ECBusinessException("This document is not available in the system");
        }
        return sampleOptional.get();
    }

    @Transactional
    protected String mergeDocumentResources(List<String> resourcePatch, String password, ECDocuments doc) throws Exception {
        List<ECDocumentResourceContract> signs = signRepository.getECDocumentResourceContractByDocumentId(doc.getId());
        ECDocumentResourceContract sign;
        String frm;
        if (CollectionUtils.isEmpty(signs)) {
            sign = new ECDocumentResourceContract();
            sign.setCompanyId(doc.getCompanyId());
            sign.setDocumentId(doc.getId());
            sign.setParentId(-1);
            String fileMerged = String.valueOf(ECGenerateCompany.toCompanyId(ECGenerateCompany.DOC, doc.getId()));
            int companyId = ECGenerateCompany.toCompanyId(ECGenerateCompany.CMP, doc.getCompanyId());
            String generated = ECGenerateCompany.generatePath(path, companyId, doc.getId());
            frm = String.format("%s/%s-%d.pdf", generated, fileMerged, ECDateUtils.currentTimeMillis());
        } else {
            sign = signs.get(0);
            frm = sign.getDocPathRaw();
        }

        fileStorageService.mergeFile(resourcePatch, password, String.format("%s/%s", path, frm));
        sign.setDocPathRaw(frm);
        sign.setDocPathSign(frm);
        sign.setCreatedBy(doc.getCreatedBy()); //Only allow creator to request create documents & merge
        sign.setStatus(true);
        signRepository.save(sign);
        //Async update resource if need.
        return frm;
    }

    private ECDocuments fetchDocument(int documentId) {
        Optional<ECDocuments> documents = documentRepository.findById(documentId);
        if (!documents.isPresent()) {
            throw new ECBusinessException("This document is not found in the system", HttpStatus.NOT_FOUND);
        }
        if (documents.get().getDeleteFlag() == 1) {
            throw new ECBusinessException("This document is not available in the system");
        }
        return documents.get();
    }

    private boolean isNeedConvert(String fileType) {
        if (Strings.isBlank(fileType)) return false;
        return docTypes.contains(fileType);
    }

    private boolean isPDF(String contentType) {
        return "application/pdf".equalsIgnoreCase(contentType);
    }

    public String getFileResource(int documentId) {
        fetchDocument(documentId);
        List<ECDocumentResourceContract> mergeDocument = signRepository.getECDocumentResourceContractByDocumentId(documentId);
        if (CollectionUtils.isEmpty(mergeDocument))
            throw new ECBusinessException("This document resource is not found");
        return mergeDocument.get(0).getDocPathSign();
    }
}
