package com.its.econtract.repository;

import com.its.econtract.entity.ECDocumentResourceContract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ECDocumentResourceSignRepository extends JpaRepository<ECDocumentResourceContract, Integer> {

    @Query("select c from ECDocumentResourceContract c where c.documentId = :documentId and c.status = true and c.deleteFlag = 0")
    List<ECDocumentResourceContract> getECDocumentResourceContractByDocumentId(@Param("documentId") int documentId);

    @Modifying
    @Query("update ECDocumentResourceContract ec set ec.docPathSign =:docPathSign where ec.documentId = :documentId")
    void updateResourceDocumentByIds(@Param("docPathSign") String docPathSign, @Param("documentId") Integer documentId);
}
