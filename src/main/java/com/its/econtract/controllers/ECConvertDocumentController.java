package com.its.econtract.controllers;

import com.its.econtract.controllers.request.DocumentRequest;
import com.its.econtract.controllers.request.UploadDocumentRequest;
import com.its.econtract.dto.EContractDto;
import com.its.econtract.facade.ECConvertDocumentFacade;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Callable;

@Log4j2
@RestController
@RequestMapping(value = "/api/v1")
public class ECConvertDocumentController {

    @Autowired
    private ECConvertDocumentFacade facade;

    @PostMapping(value = "/upload")
    public ResponseEntity uploadFiles(@Valid UploadDocumentRequest file) throws Exception {
        String packages = facade.convertPDF(file.getFile()).get();
        return EContractDto.build().withData(packages).withHttpStatus(HttpStatus.OK).toResponseEntity();
    }

    @ApiOperation(value = "Merge document requests")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Merge file process successfully."),
            @ApiResponse(code = 400, message = "Input params are not correct", response = String.class),
            @ApiResponse(code = 404, message = "Not found the document or document is deactivated", response = String.class),
            @ApiResponse(code = 415, message = "The content type is unsupported"),
            @ApiResponse(code = 500, message = "An unexpected error has occurred. The error has been logged and is being investigated.")})
    @PostMapping(value = "/document/merge")
    public ResponseEntity mergeDocument(@RequestBody @Valid DocumentRequest documentRequest) throws Exception {
        String data = facade.convertDocumentById(documentRequest).get();
        return EContractDto.build().withData(data).withHttpStatus(HttpStatus.OK).toResponseEntity();
    }

    @GetMapping(value = "/document/{documentId}")
    public Callable<ResponseEntity> getFileSignature(HttpServletRequest request, @PathVariable int documentId) {
        HttpHeaders header = new HttpHeaders();
        header.add("Cache-Control", "no-cache, no-store, must-revalidate");
        header.add("Pragma", "no-cache");
        header.add("Expires", "0");
        return () -> {
            String documentPath = facade.getFileResource(documentId);
            Path upload = Paths.get(documentPath).toAbsolutePath().normalize();
            Resource resource = new UrlResource(upload.toUri());
            String contentType = path(request, resource);
            log.info("contentType = {}", contentType);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .headers(header)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        };
    }

    private String path(HttpServletRequest request, Resource resource) throws Exception {
        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
            log.info("contentType = {}", contentType);
        } catch (IOException ex) {
            log.info("Could not determine file type.");
        }

        if (contentType == null) {
            contentType = "application/octet-stream";
        }
        return contentType;
    }
}
