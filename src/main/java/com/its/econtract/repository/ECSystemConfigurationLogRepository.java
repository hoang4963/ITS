package com.its.econtract.repository;

import com.its.econtract.entity.ECVerifyConfigurationLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ECSystemConfigurationLogRepository extends JpaRepository<ECVerifyConfigurationLog, Integer> {
}
