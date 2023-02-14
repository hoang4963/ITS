package com.its.econtract.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Setter
@Getter
@Table(name = "ec_verify_transaction_log")
@Entity
public class ECVerifyConfigurationLog extends ECBaseEntity {
    @Column(name = "content", columnDefinition = "JSON")
    private String content;
    @Column(name = "code")
    private int code;

    @Column(name = "transactionId")
    private String transactionId;
}
