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
@Table(name = "ec_document_logs")
public class ECDocumentLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "document_id")
    private int documentId;

    @Column(name = "prev_value")
    private String prevValue;

    @Column(name = "next_value")
    private String nextValue;

    @Column(name = "content")
    private String content;

    @Column(name = "action")
    private int action;

    @Column(name = "action_by")
    private String actionBy;

    @Column(name = "action_by_email")
    private String actionByEmail;

    @Column(name = "is_show", columnDefinition = "TINYINT")
    private boolean isShow;

    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "created_at")
    private Date createdAt = new Date();

    @Column(name = "updated_at")
    private Date updatedAt = new Date();
}
