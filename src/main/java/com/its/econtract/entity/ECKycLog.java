package com.its.econtract.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Setter
@Getter
@Table(name = "ec_kyc_log")
public class ECKycLog extends ECBaseEntity {
    private int code; //httpCode

    @Column(name = "api_path")
    private String apiPath;

    @Column(name = "content", columnDefinition = "JSON")
    private String content;

    @Column(name = "company_id")
    private int companyId;

    @Column(name = "type", columnDefinition = "TINYINT")
    private int kycType;

    @Column(name = "object_id")
    private int objectId; // Cái này có thể là accountId hoặc documentId nhe

    @Column(name = "vendor")
    private String vendor;

    @Column(name = "start_time")
    private long startTime = 0;

    @Column(name = "end_time")
    private long endTime = 0;
}
