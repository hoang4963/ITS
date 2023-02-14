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
public class OCRFrontResponseDto {
    private String id;
    private String name;
    private String birthday;
    private String sex;
    private String hometown;
    private String address;

    public OCRFrontResponseDto getOCRFrontResponseDto(OCRResponse ocrResponse){
        OCRFrontResponseDto ocrFrontResponseDto = new OCRFrontResponseDto();
        ocrFrontResponseDto.setId(ocrResponse.getId());
        ocrFrontResponseDto.setName(ocrResponse.getName());
        ocrFrontResponseDto.setBirthday(ocrResponse.getBirthday());
        ocrFrontResponseDto.setSex(ocrResponse.getSex());
        ocrFrontResponseDto.setHometown(ocrResponse.getHometown());
        ocrFrontResponseDto.setAddress(ocrResponse.getAddress());

        return ocrFrontResponseDto;
    }
}
