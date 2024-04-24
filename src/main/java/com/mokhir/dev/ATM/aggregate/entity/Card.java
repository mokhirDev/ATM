package com.mokhir.dev.ATM.aggregate.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.format.annotation.NumberFormat;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "card")
public class Card implements Serializable {
    @Serial
    private static final long serialVersionUID = -7330543945961999344L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "checkCardQuantity must not be empty")
    private Integer checkCardQuantity;

    @NotNull(message = "balance must not be empty")
    @Min(value = 0, message = "Balance cannot be less than zero")
    private Double balance;

    @NotNull(message = "card cvc must not be empty")
    @Size(min = 3, max = 3, message = "CVC must be 3 symbols")
    private String cardCvc;

    @NotNull(message = "cardExpireDate must not be empty")
    @Future(message = "Expiry date must be in the future")
    private LocalDate cardExpireDate;

    @NumberFormat
    @NotNull(message = "card number must not be empty")
    @Column(unique = true, nullable = false)
    @Size(min = 16, max = 16, message = "Card number must contains 16 digits long")
    private String cardNumber;

    private Integer cardPin;

    @NotNull(message = "card must be active by default")
    private Boolean isActive;

    @ManyToOne
    @NotNull(message = "User have to value")
    @JoinColumn(name = "cardholder_id")
    private CardHolder cardHolder;

    @ManyToOne
    @NotNull(message = "Type of card must not be empty")
    @JoinColumn(name = "card_type_id")
    private CardType cardType;
}
