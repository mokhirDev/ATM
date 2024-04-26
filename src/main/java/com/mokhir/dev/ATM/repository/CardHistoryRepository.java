package com.mokhir.dev.ATM.repository;

import com.mokhir.dev.ATM.aggregate.entity.Card;
import com.mokhir.dev.ATM.aggregate.entity.HistoryCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface CardHistoryRepository extends JpaRepository<HistoryCard, Long> {
    List<HistoryCard> findAllByFromCardOrToCard(Card fromCard, Card toCard);
}

