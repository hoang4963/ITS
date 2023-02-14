package com.its.econtract.controllers.request;

import com.its.econtract.anotation.ECDocumentAllow;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Setter
@Getter
@ApiModel(description = "Document request")
public class UploadDocumentRequest {

    @ApiModelProperty(value = "file", notes = "File upload support: doc|docx you want to convert")
    @ECDocumentAllow.FileValidator(message = "Invalid document types")
    private MultipartFile file = null;

    private boolean isKeepOrigin = true;
}
