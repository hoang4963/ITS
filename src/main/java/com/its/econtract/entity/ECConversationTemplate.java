package com.its.econtract.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Setter
@Getter
@Table(name = "ec_s_conversation_templates")
public class ECConversationTemplate  extends ECBaseEntity{
    @Column(name = "template_name")
    private String name;

    @Column(name = "template_description")
    private String description;

    @Column(name = "template")
    private String template;

    @Column(name = "type", columnDefinition = "TINYINT")
    private int type;

    @Column(name = "status", columnDefinition = "TINYINT")
    private int status;

    @Column(name = "is_ams", columnDefinition = "TINYINT")
    private int isAms;
}
