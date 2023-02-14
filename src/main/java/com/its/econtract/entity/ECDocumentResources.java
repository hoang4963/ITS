package com.its.econtract.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Setter
@Getter
@Entity
@Table(name = "ec_document_resources")
public class ECDocumentResources extends ECBaseEntity {
    @Column(name = "document_id")
    private int documentId;

    @Column(name = "parent_id")
    private int parentId;

    @Column(name = "file_name_raw")
    private String fileNameRaw;

    @Column(name = "file_type_raw")
    private String fileTypeRaw;

    @Column(name = "file_size_raw")
    private String fileSizeRaw;

    @Column(name = "file_path_raw")
    private String filePathRaw;

    @Column(name = "status")
    private boolean status;

    @Column(name = "version")
    private int version;

    @Column(name = "note")
    private String note;

    @Column(name = "created_by")
    private int createdBy;

    @Column(name = "updated_by")
    private int updatedBy;
}
