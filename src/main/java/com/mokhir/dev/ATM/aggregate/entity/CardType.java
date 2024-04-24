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
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"name", "number"})
})
public class CardType implements Serializable {
    @Serial
    private static final long serialVersionUID = -3322182684770442620L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;
    @Column(unique = true, nullable = false)
    private String number;
    @ManyToOne
    @JoinColumn(name = "currency_type_id")
    private CurrencyType currencyType;
    private Integer expirationYear;
}
