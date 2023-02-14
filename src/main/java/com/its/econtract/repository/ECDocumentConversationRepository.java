package com.its.econtract.repository;


import com.its.econtract.entity.ECDocumentConversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ECDocumentConversationRepository extends JpaRepository<ECDocumentConversation, Integer> {
	 @Query("select a from ECDocumentConversation a where a.docId = :documentId and a.templateId = :templateId and a.status = 1")
	 List<ECDocumentConversation> getRemindedECDocumentByDocIdAndTemplateId(@Param("documentId") int documentId, @Param("templateId") int templateId);
}
