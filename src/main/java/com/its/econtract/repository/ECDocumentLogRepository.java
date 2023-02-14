package com.its.econtract.repository;

import com.its.econtract.entity.ECDocumentLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ECDocumentLogRepository extends JpaRepository<ECDocumentLog, Integer> {
}
