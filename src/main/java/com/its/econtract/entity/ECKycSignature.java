package com.its.econtract.entity;

import com.its.econtract.dto.KycSignatureDto;

import java.util.List;

public interface ECKycSignature {
    List<KycSignatureDto> getKycByDocumentId(int documentId);
}
