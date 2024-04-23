package com.mokhir.dev.ATM.repository;

import com.mokhir.dev.ATM.aggregate.entity.CurrencyType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CurrencyTypeRepository extends JpaRepository<CurrencyType, Long> {
}
