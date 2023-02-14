package com.its.econtract.repository;

import com.its.econtract.entity.ECDocumentAssignee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
@Transactional
public interface ECDocumentAssigneeRepository extends JpaRepository<ECDocumentAssignee, Integer> {
    @Query("select a from ECDocumentAssignee a where a.documentId = :documentId and a.status = 1 and a.assignType in (:roles) or a.id = :id")
    List<ECDocumentAssignee> getReminderAssigneeByDocumentIdAndId(@Param("documentId") int documentId, @Param("roles") List<Integer> roleId, @Param("id") Integer id);

    @Query("select a from ECDocumentAssignee a where a.documentId = :documentId and a.status = 1 and a.id in (:ids)")
    List<ECDocumentAssignee> getReminderAssigneeByIds(@Param("documentId") int documentId, @Param("ids") List<Integer> ids);

    @Query("select a from ECDocumentAssignee a where a.documentId = :documentId and a.status = 1")
    List<ECDocumentAssignee> getReminderAssignees(@Param("documentId") int documentId);

    @Query("select a from ECDocumentAssignee a where a.documentId = :documentId and a.status = 1 and a.assignType in (:assignType)")
    List<ECDocumentAssignee> getReminderAssigneeByAssignType(@Param("documentId") int documentId, @Param("assignType") List<Integer> ids);

    @Modifying
    @Query("update ECDocumentAssignee a set a.state = :state where a.id = :id")
    void updateState(@Param(value = "id") int id, @Param(value = "state") int state);
}
