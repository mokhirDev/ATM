package com.mokhir.dev.ATM.repository;

import com.mokhir.dev.ATM.aggregate.entity.CardType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CardTypeRepository extends JpaRepository<CardType, Long> {
}
