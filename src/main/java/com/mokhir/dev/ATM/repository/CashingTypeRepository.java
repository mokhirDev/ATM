package com.mokhir.dev.ATM.repository;

import com.mokhir.dev.ATM.aggregate.entity.CashingType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CashingTypeRepository extends JpaRepository<CashingType, Long> {
}
