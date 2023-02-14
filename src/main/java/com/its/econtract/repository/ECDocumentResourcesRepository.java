package com.its.econtract.repository;

import com.its.econtract.entity.ECDocumentResources;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ECDocumentResourcesRepository extends JpaRepository<ECDocumentResources, Integer> {

    @Query("select rs from ECDocumentResources rs where rs.documentId = :documentId and rs.status = true order by rs.id asc")
    List<ECDocumentResources> fetchDocumentResources(@Param("documentId") int documentId);
}
