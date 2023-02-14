package com.its.econtract.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Setter
@Getter
@Entity
@Table(name = "ec_document_text_info")
public class ECDocumentTextInfos extends ECBaseEntity {
    @Column(name = "document_id")
    private int documentId;

    @Column(name = "matruong")
    private String ma;

    @Column(name = "content")
    private String content;

    @Column(name = "font_size")
    private int fontSize;

    @Column(name = "font_style")
    private String fontStyle;

    @Column(name = "page_sign")
    private int pageSign;

    @Column(name = "width_size")
    private float withSize;

    @Column(name = "height_size")
    private float heightSize;

    @Column(name = "x")
    private float x;

    @Column(name = "y")
    private float y;

    @Column(name = "page_width")
    private float pageWidth;

    @Column(name = "page_height")
    private float pageHeight;

    @Column(name = "data_type")
    private int dataType;

    @Column(name = "created_by")
    private int createdBy;

    @Column(name = "updated_by")
    private int updatedBy;
}
