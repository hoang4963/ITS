package com.its.econtract.entity;

import com.its.econtract.entity.converter.ECConverter;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Setter
@Getter
@Entity
@Table(name = "ec_s_send_email")
public class ECSendEmail extends ECBaseEntity {
    @Column(name = "company_id")
    private int companyId;

    @Column(name = "email_host")
    private String host;

    @Column(name = "email_address")
    private String address;

    @Column(name = "email_password")
    @Convert(converter = ECConverter.class)
    private String password;

    @Column(name = "email_name")
    private String name;

    @Column(name = "port")
    private int port;

    @Column(name = "email_protocol")
    @Transient
    private String protocol = "smtp";

    @Column(name = "is_use_ssl", columnDefinition = "TINYINT")
    private int ssl;

    @Column(name = "status", columnDefinition = "TINYINT")
    private boolean status;

    @Column(name = "created_by")
    private int createdBy;

    @Column(name = "updated_by")
    private int updatedBy;
}
