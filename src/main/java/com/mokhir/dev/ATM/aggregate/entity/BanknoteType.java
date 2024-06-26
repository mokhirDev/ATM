package com.mokhir.dev.ATM.aggregate.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class BanknoteType implements Serializable, Comparable<BanknoteType> {

    @Serial
    private static final long serialVersionUID = 2622248991980100502L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String name;
    @Column(nullable = false, unique = true)
    private Integer nominal;
    private Integer quantity;
    @ManyToOne
    @JoinColumn(name = "cashing_type_id")
    private CashingType cashingTypeId;
    @ManyToOne
    @JoinColumn(name = "currency_type_id")
    private CurrencyType currencyTypeId;
    @Override
    public int compareTo(BanknoteType o) {
        return o.getNominal()-this.nominal;
    }
}
