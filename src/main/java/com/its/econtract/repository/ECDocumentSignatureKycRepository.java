package com.its.econtract.repository;

import com.its.econtract.entity.ECDocumentSignatureKyc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ECDocumentSignatureKycRepository extends JpaRepository<ECDocumentSignatureKyc, Integer> {
    @Query("select kc from ECDocumentSignatureKyc kc where kc.assignId = :assignId")
    ECDocumentSignatureKyc findDocumentSignImage(@Param("assignId") int assignId);

    @Query("select kc from ECDocumentSignatureKyc kc where kc.assignId in (select a.id from ECDocumentAssignee as a where a.documentId = :documentId)")
    List<ECDocumentSignatureKyc> getKycCA(@Param("documentId") int documentId);
}
