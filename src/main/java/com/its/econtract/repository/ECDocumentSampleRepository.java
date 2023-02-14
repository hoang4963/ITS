package com.its.econtract.repository;

import com.its.econtract.entity.ECDocumentSample;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ECDocumentSampleRepository extends JpaRepository<ECDocumentSample, Integer> {

    @Query("select c from ECDocumentSample c where c.documentTypeId = :documentTypeId and c.deleteFlag = 0")
    List<ECDocumentSample> getECDocumentResourceContractByDocumentId(@Param("documentTypeId") int documentTypeId);

}
