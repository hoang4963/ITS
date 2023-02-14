package com.its.econtract.repository;

import com.its.econtract.entity.VtpCustomerInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VtpCustomerInfoRepository  extends JpaRepository<VtpCustomerInfo,Integer> {
    @Query("select c from VtpCustomerInfo c where c.customerId = :customerId")
    VtpCustomerInfo findCustomerImage(@Param("customerId") int customerId);
}
