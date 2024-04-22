package com.mokhir.dev.ATM.aggregate.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class CardType implements Serializable {
    @Serial
    private static final long serialVersionUID = 2064366164899764844L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    private Integer number;
    private Integer expiration_year;
    @ManyToOne
    @JoinColumn(name = "currency_type_id")
    private CurrencyType currencyType;
}
