package com.its.econtract.repository;

import com.its.econtract.entity.ECDocuments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
@Transactional
public interface ECDocumentRepository extends JpaRepository<ECDocuments, Integer> {

    @Modifying
    @Query("update ECDocuments ec set ec.documentState =:state, ec.updatedAt = :updatedAt where ec.id IN :listOfId")
    void updateStateDocumentByIds(@Param("state") int state, @Param("updatedAt") Date updatedAt, @Param("listOfId") List<Integer> listOfId);

    @Query("select d from ECDocuments d where d.transactionId = :transactionId")
    List<ECDocuments> getDocumentByTransactionId(@Param("transactionId") String transactionId);
}
