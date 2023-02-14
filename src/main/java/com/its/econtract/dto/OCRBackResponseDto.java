package com.its.econtract.dto;

import com.its.econtract.controllers.response.OCRResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class OCRBackResponseDto {
    private String issueDate;
    private String issueBy;

    public OCRBackResponseDto getOCRBackResponseDto(OCRResponse ocrResponse){
        OCRBackResponseDto ocrBackResponseDto = new OCRBackResponseDto();
        ocrBackResponseDto.setIssueDate(ocrResponse.getIssueDate());
        ocrBackResponseDto.setIssueBy(ocrResponse.getIssueBy());
        return ocrBackResponseDto;
    }
}
