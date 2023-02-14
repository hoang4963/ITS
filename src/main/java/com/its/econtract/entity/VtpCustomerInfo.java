package com.its.econtract.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Setter
@Getter
@Table(name = "vtp_customer_info")
@Entity
public class VtpCustomerInfo extends ECBaseEntity{
    @Column(name = "customer_id")
    private Integer customerId;

    @Column(name = "front_image_url")
    private String frontImage;

    @Column(name = "back_image_url")
    private String backImage;

}
