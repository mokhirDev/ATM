package com.mokhir.dev.ATM.aggregate.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class HistoryCard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @DecimalMin(value = "0", message = "Amount must be greater than or equal to 0")
    @DecimalMax(value = "10000000", message = "Amount must be less than or equal to 10000000")
    private BigDecimal amount;
    private BigDecimal commission;
    private LocalDateTime date;
    @ManyToOne
    @JoinColumn(name = "from_card_id")
    private Card fromCard;
    @ManyToOne
    @JoinColumn(name = "to_card_id")
    private Card toCard;
}
