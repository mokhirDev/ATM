package com.mokhir.dev.ATM.repository;

import com.mokhir.dev.ATM.aggregate.entity.CardHolder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CardHolderRepository extends JpaRepository<CardHolder, Long> {
    CardHolder findByPinFl(String cardNumber);
}
