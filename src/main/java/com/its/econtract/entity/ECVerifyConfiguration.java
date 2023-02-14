package com.its.econtract.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;
import java.sql.Blob;

@Setter
@Getter
@Entity
@Table(name = "ec_everify_configuration")
@ToString
public class ECVerifyConfiguration extends ECBaseEntity {
    @Lob
    @Column(name = "pub_key", columnDefinition="BLOB")
    public byte[] publicKey;

    @Column(name = "active", columnDefinition = "TINYINT")
    private boolean active;
}
