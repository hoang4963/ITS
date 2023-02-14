package com.its.econtract.repository;

import com.its.econtract.entity.ECDocumentPartners;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ECDocumentPartnerRepository extends JpaRepository<ECDocumentPartners, Integer> {
    @Query("select p from ECDocumentPartners p where p.documentId = :documentId")
    List<ECDocumentPartners> getECDocumentPartnersByDocumentId(@Param("documentId") int documentId);
}
