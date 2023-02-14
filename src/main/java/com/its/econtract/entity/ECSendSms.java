package com.its.econtract.entity;

import com.its.econtract.entity.converter.ECConverter;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Table;

@Setter
@Getter
@Entity
@Table(name = "ec_s_send_sms")
public class ECSendSms extends ECBaseEntity {
    @Column(name = "company_id")
    private int companyId;

    @Column(name = "service_provider")
    private String provider;

    @Column(name = "service_url")
    private String url;

    @Column(name = "brandname")
    private String brandname;

    @Column(name = "sms_account")
    private String account;

    @Column(name = "sms_password")
    @Convert(converter = ECConverter.class)
    private String password;

    @Column(name = "status", columnDefinition = "TINYINT")
    private boolean status;

    @Column(name = "created_by")
    private int createdBy;

    @Column(name = "updated_by")
    private int updatedBy;
}
