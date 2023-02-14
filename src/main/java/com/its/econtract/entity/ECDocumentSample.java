package com.its.econtract.entity;


import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Setter
@Getter
@Entity
@Table(name = "ec_s_document_samples")
public class ECDocumentSample extends ECBaseEntity {
    @Column(name = "company_id")
    private Integer companyId;

    @Column(name = "document_type")
    private Integer documentType;

    @Column(name = "document_type_id")
    private Integer documentTypeId;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String desc;

    @Column(name = "is_verify_content")
    private boolean isVerifyContent;

    @Column(name = "document_path_original")
    private String docPathRaw;

    @Column(name = "created_by")
    private int createdBy;

    @Column(name = "updated_by")
    private int updateBy;

    @Column(name = "delete_flag")
    private int deleteFlag;
}

