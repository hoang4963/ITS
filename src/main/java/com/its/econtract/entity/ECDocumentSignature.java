package com.its.econtract.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Setter
@Getter
@Entity
@Table(name = "ec_document_signature")
public class ECDocumentSignature extends ECBaseEntity {

    @Column(name = "document_id")
    private int documentId;

    @Column(name = "assign_id")
    private int assignId;

    @Column(name = "page_sign")
    private int pageSign;

    @Column(name = "width_size")
    private float widthSize;

    @Column(name = "height_size")
    private float heightSize;

    @Column(name = "page_height")
    private float pageHeight;

    @Column(name = "page_width")
    private float pageWidth;

    @Column(name = "x")
    private float x;

    @Column(name = "y")
    private float y;

    @Column(name = "created_by")
    private int createdBy;

    @Column(name = "updated_by")
    private int updatedBy;
}
