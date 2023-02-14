package com.its.econtract.dto;

import com.its.econtract.controllers.response.KYCResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.text.DecimalFormat;
import java.text.NumberFormat;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class KYCResponseDto {
    private Double sim;

    public KYCResponseDto getKYCResponseDto(KYCResponse kycResponse){
        KYCResponseDto kycResponseDto = new KYCResponseDto();
        kycResponseDto.setSim(kycResponse.getSim());
        return kycResponseDto;
    }

    public void setSim(Double sim) {
        NumberFormat formatter = new DecimalFormat("#0.00");
        this.sim = Double.valueOf(formatter.format(sim*100));
    }
}
