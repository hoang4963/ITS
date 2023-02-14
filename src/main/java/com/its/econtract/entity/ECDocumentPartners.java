package com.its.econtract.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "ec_document_partners")
@Setter
@Getter
@ToString
public class ECDocumentPartners extends ECBaseEntity {
    @Column(name = "document_id")
    private int documentId;

    @Column(name = "order_assignee")
    private int orderAssignee;

    @Column(name = "organisation_type")
    private int orgType;

    @Column(name = "organisation_name")
    private String orgName;

    @Column(name = "code")
    private String code;

    @Column(name = "tax")
    private String tax;
}
