package com.mokhir.dev.ATM.repository;

import com.mokhir.dev.ATM.aggregate.entity.Card;
import com.mokhir.dev.ATM.aggregate.entity.HistoryCard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Repository
public interface CardHistoryRepository extends JpaRepository<HistoryCard, Long> {
    Page<HistoryCard> findAllByFromCardOrToCard(Card fromCard, Card toCard, Pageable pageable);
    Page<HistoryCard> findAllByDateLike(LocalDateTime date, Pageable pageable);

}

