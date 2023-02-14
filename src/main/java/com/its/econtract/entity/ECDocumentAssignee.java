package com.its.econtract.entity;

import com.its.econtract.entity.converter.ECConverter;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Entity
@Setter
@Getter
@Table(name = "ec_document_assignees")
public class ECDocumentAssignee extends ECBaseEntity {
    @Column(name = "company_id")
    private int companyId;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "email")
    private String email;

    @Column(name = "phone")
    private String phone;

    @Column(name = "address")
    private String address;

    @Column(name = "ext_info")
    private String extInfo;

    @Column(name = "document_id")
    private int documentId;

    @Column(name = "partner_id")
    private int partnerId;

    @Column(name = "message")
    private String message;

    @Column(name = "noti_type")
    private int notiType;

    @Column(name = "`order`", columnDefinition = "TINYINT")
    private int order;

    @Column(name = "status", columnDefinition = "TINYINT")
    private int status;

    @Column(name = "is_internal")
    private boolean internal;

    @Column(name = "state", columnDefinition = "TINYINT") //2 PDF
    private int state;

    @Column(name = "reason")
    private String reason;

    @Column(name = "submit_time")
    private Date submitTime;

    @Column(name = "assign_type", columnDefinition = "TINYINT")
    private int assignType;

    @Column(name = "is_required")
    private boolean required;

    @Column(name = "password")
    private String password;

    @Column(name = "url_code")
    private String urlCode;

    @Column(name = "otp")
    private String otp;

    @Column(name = "national_id")
    private String identifyNumber;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "credential_id")
    private String credentialID;

    @Transient
    private String country = "Vietnam";

    @Transient
    private String location;
}
