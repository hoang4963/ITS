package com.its.econtract.repository;

import com.its.econtract.entity.ECDocumentTypes;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ECDocumentTypeRepository extends JpaRepository<ECDocumentTypes, Integer> {
}
