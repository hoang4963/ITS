package com.its.econtract.repository;

import com.its.econtract.entity.ECDocumentSignature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ECDocumentSignatureRepository extends JpaRepository<ECDocumentSignature, Integer> {

    @Query("select sign from ECDocumentSignature sign where sign.documentId = :documentId and sign.assignId = :assignId")
    List<ECDocumentSignature> getECDocumentSignature(@Param("documentId") int documentId, @Param("assignId") int assignId);
}
