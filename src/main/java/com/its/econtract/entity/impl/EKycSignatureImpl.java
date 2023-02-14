package com.its.econtract.entity.impl;

import com.its.econtract.dto.KycSignatureDto;
import com.its.econtract.entity.ECDocumentAssignee;
import com.its.econtract.entity.ECDocumentSignatureKyc;
import com.its.econtract.entity.ECKycSignature;
import com.its.econtract.repository.ECDocumentAssigneeRepository;
import com.its.econtract.repository.ECDocumentSignatureKycRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Log4j2
@Service
public class EKycSignatureImpl implements ECKycSignature {

    @Autowired
    private ECDocumentSignatureKycRepository kycRepository;

    @Autowired
    private ECDocumentAssigneeRepository assigneeRepository;

    @Override
    public List<KycSignatureDto> getKycByDocumentId(int documentId) {
        List<ECDocumentSignatureKyc> kycs = kycRepository.getKycCA(documentId);
        List<ECDocumentAssignee> assignees = assigneeRepository.getReminderAssignees(documentId);
        Map<Integer, ECDocumentAssignee> map = assignees.stream()
                .collect(Collectors.toMap(ECDocumentAssignee::getId, Function.identity()));
        List<KycSignatureDto> emails = kycs.stream()
                .map(item -> {
                    ECDocumentAssignee as = map.get(item.getAssignId());
                    return new KycSignatureDto(item.getCertificate(), item.getPrivateKey(), item.getPubKey(), as);
                })
                .collect(Collectors.toList());
        return emails;
    }
}
