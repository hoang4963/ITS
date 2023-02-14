package com.its.econtract.dto;

import com.its.econtract.entity.ECDocumentAssignee;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@Data
@AllArgsConstructor
@NoArgsConstructor
public class KycSignatureDto {
    private byte[] certificate;
    private byte[] pri;
    private byte[] pub;
    private ECDocumentAssignee assignee;
}
