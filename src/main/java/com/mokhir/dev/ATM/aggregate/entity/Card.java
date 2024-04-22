package com.mokhir.dev.ATM.aggregate.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
public class Card implements Serializable {
    @Serial
    private static final long serialVersionUID = -7330543945961999344L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "int default 3")
    private Integer checkCardQuantity;

    @Positive(message = "Balance must have positive value")
    @Column(columnDefinition = "double default 0.0")
    private Double balance;

    @NotNull
    @Size(min = 3, max = 4, message = "CVC must be from 3 to 4 symbols")
    private String card_cvc;

    @NotNull
    @Pattern(regexp = "\\d{4}-\\d{2}", message = "Неправильный формат даты и времени")
    @JsonFormat(pattern = "yyyy-MM", timezone = "Asia/Tashkent")
    private String card_expire_date;

    @NotNull
    @Column(unique = true, nullable = false)
    @JsonFormat(locale = "card_number")
    private String cardNumber;

    @NotNull
    @Size(min = 4, max = 4, message = "Пин-код должен быть длиной 4 символа")
    private String card_pin;

    @NotNull
    @Column(columnDefinition = "boolean default true")
    private Boolean is_active;

    @NotNull(message = "User have to value")
    @ManyToOne
    @JoinColumn(name = "cardholder_id")
    private CardHolder user;

    @NotNull(message = "Type of card must not be empty")
    @ManyToOne
    @JoinColumn(name = "card_type_id")
    private CardType cardType;
}
