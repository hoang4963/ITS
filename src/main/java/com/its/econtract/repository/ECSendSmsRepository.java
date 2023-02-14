package com.its.econtract.repository;

import com.its.econtract.entity.ECSendSms;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ECSendSmsRepository extends JpaRepository<ECSendSms, Integer> {

    @Query("select e from ECSendSms e where e.companyId = :companyId and e.status = true ")
    ECSendSms getECSendSmsByCompanyId(@Param("companyId") int companyId);

    @Query("select e from ECSendSms e where e.companyId IN (:companyIds) and e.status = true ")
    List<ECSendSms> getECSendSmsByCompanyIds(@Param("companyIds") List<Integer> companyIds);
}
