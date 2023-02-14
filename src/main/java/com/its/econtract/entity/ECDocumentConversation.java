package com.its.econtract.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Setter
@Getter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "ec_document_conversations")
public class ECDocumentConversation extends ECBaseEntity {
    @Column(name = "company_id")
    private Integer companyId;

    @Column(name = "document_id")
    private Integer docId;

    @Column(name = "notify_type")
    private Integer notifyType;

    @Column(name = "send_id")
    private Integer sendId ;

    @Column(name = "send_type")
    private Integer sendType;

    @Column(name = "content")
    private String content;

    @Column(name = "template_id")
    private Integer templateId;

    @Column(name = "status", columnDefinition = "TINYINT")
    private Integer status;

    @Column(name = "delete_flag", columnDefinition = "TINYINT")
    private Integer deleteFlag = 0;

    public ECDocumentConversation(int companyId, int documentId, int templateId, int notifyType, int sendType, String content, int status, int sendId) {
        this.companyId = companyId;
        this.docId = documentId;
        this.templateId = templateId;
        this.notifyType = notifyType;
        this.sendType = sendType;
        this.content = content;
        this.status = status;
        this.sendId = sendId;

    }
}
