package com.its.econtract.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Setter
@Getter
@Entity
@Table(name = "ec_s_document_sample_resources")
public class ECDocumentSampleResources extends ECBaseEntity {
    @Column(name = "document_sample_id")
    private Integer documentSampleId;

    @Column(name = "file_name_raw")
    private String fileNameRaw;

    @Column(name = "file_type_raw")
    private String fileTypeRaw;

    @Column(name = "file_size_raw")
    private String fileSizeRaw;

    @Column(name = "file_path_raw")
    private String filePathRaw;

    @Column(name = "file_id")
    private String fileId;

    @Column(name = "status")
    private boolean status;

    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "updated_by")
    private Integer updatedBy;
}
