package com.its.econtract.repository;

import com.its.econtract.entity.ECVerifyConfiguration;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ECVerifyConfigurationRepository extends JpaRepository<ECVerifyConfiguration, Integer> {
    @Query("select c from ECVerifyConfiguration c where c.active = true ")
    List<ECVerifyConfiguration> getActiveSignature();
}
