package com.its.econtract.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Setter
@Getter
@Entity
@Table(name = "s_document_types")
public class ECDocumentTypes extends ECBaseEntity{
    @Column(name = "dc_type_code")
    private String typeCode;

    @Column(name = "dc_type_name")
    private String typeName;
}
