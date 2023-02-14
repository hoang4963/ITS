package com.its.econtract.repository;

import com.its.econtract.entity.ECDocumentTextInfos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ECDocumentTextInfosRepository extends JpaRepository<ECDocumentTextInfos, Integer> {
    @Query("select rc from ECDocumentTextInfos rc where rc.documentId = :documentId")
    List<ECDocumentTextInfos> getInfo(@Param("documentId") int documentId);
}
