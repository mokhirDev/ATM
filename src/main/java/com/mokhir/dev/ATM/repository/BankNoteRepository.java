package com.mokhir.dev.ATM.repository;

import com.mokhir.dev.ATM.aggregate.entity.BanknoteType;
import com.mokhir.dev.ATM.aggregate.entity.CashingType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BankNoteRepository extends JpaRepository<BanknoteType, Long> {
    List<BanknoteType> findAllByCashingTypeId(CashingType cashingType);
    BanknoteType findByNominal(Integer nominal);
}
