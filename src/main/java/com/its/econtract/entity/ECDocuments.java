package com.its.econtract.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;

@Setter
@Getter
@Table(name = "ec_documents")
@Entity
public class ECDocuments extends ECBaseEntity {

    @Column(name = "company_id")
    private int companyId;

    @Column(name = "document_type_id")
    private int docTypeId;

    @Column(name = "status")
    private int status;

    @Column(name = "is_order_approval")
    private boolean isOrder;

    @Column(name = "is_verify_content")
    private boolean isVerifyContent;

    @Column(name = "is_request_confirmed")
    private boolean isRequestConfirmed;

    @Column(name = "is_request_org_confirmed")
    private boolean isRequestOrgConfirmed;

    @Column(name = "document_draft_state")
    private int documentDraftState;

    @Column(name = "document_state")
    private int documentState;

    @Column(name = "sent_date")
    private Date sentDate;

    @Column(name = "expired_date")
    private Date expiredDate;

    @Column(name = "finished_date")
    private Date finishedDate;

    @Column(name = "name")
    private String name;

    @Column(name = "code")
    private String code;

    @Column(name = "description")
    private String description;

    @Column(name = "transaction_id")
    private String transactionId;

    @Column(name = "version")
    private int version;

    @Column(name = "source")
    private Integer source;

    @Column(name = "expired_type")
    private Integer expiredType;

    @Column(name = "doc_expired_date")
    private Date docExpiredDate;

    @Column(name = "expired_month")
    private Integer expiredMonth;

    @Column(name = "parent_id")
    private Integer parentId;

    @Column(name = "addendum_type")
    private Integer addendumType;

    @Column(name = "delete_flag")
    private int deleteFlag;
    
    @Column(name = "remimder_type")
    private int remimderType;

    @Column(name = "current_assignee_id")
    private Integer currentAssigneeId;

    @Column(name = "created_by")
    private int createdBy;

    @Column(name = "updated_by")
    private int updatedBy;
}
