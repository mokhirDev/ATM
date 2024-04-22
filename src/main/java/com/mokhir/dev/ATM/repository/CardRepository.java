package com.mokhir.dev.ATM.repository;

import com.mokhir.dev.ATM.aggregate.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {
}
