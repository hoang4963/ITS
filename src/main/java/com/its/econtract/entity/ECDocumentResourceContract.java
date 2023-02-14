package com.its.econtract.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

@Setter
@Getter
@Entity
@Table(name = "ec_document_resources_ex")
public class ECDocumentResourceContract extends ECBaseEntity {
    @Column(name = "company_id")
    private int companyId;

    @Column(name = "document_id")
    private int documentId;

    @Column(name = "parent_id")
    private int parentId = -1;

    @Column(name = "document_path_original")
    private String docPathRaw;

    @Column(name = "document_path_sign")
    private String docPathSign;

    @Column(name = "created_by")
    private int createdBy;

    @Column(name = "status")
    private boolean status;

    @Column(name = "delete_flag")
    private int deleteFlag;

    @Column(name = "hash", length = 255, nullable = true)
    private String hashContent;

    @Transient
    private byte[] hashContent_;
}
