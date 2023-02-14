package com.its.econtract.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Setter
@Getter
@Entity
@Table(name = "ec_truc_feedback")
public class ECTrucFeedback {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "transactionId")
    private String transactionId;

    @Column(name = "verificationCode")
    private String verificationCode;

    @Column(name = "feedback", columnDefinition = "JSON")
    private String content;

    @Column(name = "state", columnDefinition = "TINYINT")
    private int state;

    @Column(name = "max_doing")
    private int maxDoing = 1;

    @Column(name = "created_at")
    private Date createdAt = new Date();
}
