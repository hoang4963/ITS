package com.its.econtract.controllers.request;

import com.its.econtract.anotation.ECDocumentAllow;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class OCRRequest {
    @ApiModelProperty(value = "file", notes = "File upload support: png|jpeg")
    @ECDocumentAllow.FileValidator(types = {"png","jpg","jpeg","jpe"}, checkContentType = false ,message = "Invalid file types")
    private MultipartFile file;
}
