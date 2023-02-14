package com.its.econtract.repository;

import com.its.econtract.entity.ECDocumentSampleResources;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ECDocumentSampleResourcesRepository extends JpaRepository<ECDocumentSampleResources, Integer> {

    @Query("select rs from ECDocumentSampleResources rs where rs.documentSampleId = :documentSampleId and rs.status = true order by rs.id asc")
    List<ECDocumentSampleResources> fetchDocumentSampleResources(@Param("documentSampleId") int documentSampleId);
}
