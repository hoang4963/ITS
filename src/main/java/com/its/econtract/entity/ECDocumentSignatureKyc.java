package com.its.econtract.entity;


import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;
import java.util.Date;

@Setter
@Getter
@Entity
@Table(name = "ec_document_signature_kyc")
public class ECDocumentSignatureKyc extends ECBaseEntity {

    @Column(name = "assign_id")
    private Integer assignId;

    @Column(name = "image_signature")
    private String img;

    @Column(name = "front_image_url")
    private String frontImage;

    @Column(name = "back_image_url")
    private String backImage;

    @Column(name = "sign_type")
    private Integer signType;

    @Lob
    @Column(name = "x509_certificate")
    private byte[] certificate;

    @Lob
    @Column(name = "pri_key")
    private byte[] privateKey;

    @Lob
    @Column(name = "pub_key")
    private byte[] pubKey;

    @Column(name = "signed_at")
    private Date signedAt;

    @Column(name = "face_image_url")
    private String faceImage;

    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "updated_by")
    private Integer updatedBy;
}
