package com.its.econtract.repository;

import com.its.econtract.entity.ECSendEmail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ECSendEmailRepository extends JpaRepository<ECSendEmail, Integer> {
    @Query("select e from ECSendEmail e where e.companyId = :companyId and e.status = true")
    ECSendEmail getEmailSettingByCompany(@Param("companyId") int companyId);

    @Query("select e from ECSendEmail e where e.companyId in (:companyIds) and e.status = true")
    List<ECSendEmail> getEmailSettingByCompanyIds(@Param("companyIds") List<Integer> companyIds);
}
