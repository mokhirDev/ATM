package com.mokhir.dev.ATM.aggregate.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
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
    @Column(columnDefinition = "int default 3")
    private Integer checkCardQuantity;

    @NotNull(message = "balance must not be empty")
    @Column(columnDefinition = "double default 0.0")
    @Min(value = 0, message = "Balance cannot be less than zero")
    private Double balance;

    @NotNull(message = "card cvc must not be empty")
    @Size(min = 3, max = 3, message = "CVC must be 3 symbols")
    private String cardCvc;

    @NotNull(message = "cardExpireDate must not be empty")
    @Pattern(regexp = "\\d{4}-\\d{2}", message = "Incorrect date and time format")
    @JsonFormat(pattern = "yyyy-MM", timezone = "Asia/Tashkent")
    private LocalDate cardExpireDate;

    @NumberFormat
    @NotNull(message = "card number must not be empty")
    @Column(unique = true, nullable = false)
    @Size(min = 16, max = 16)
    private String cardNumber;

    private Integer cardPin;

    @NotNull(message = "card must be true by default")
    @Column(columnDefinition = "boolean default true")
    private Boolean isActive;

    @ManyToOne
    @NotNull(message = "User have to value")
    @JoinColumn(name = "cardholder_id")
    private CardHolder user;

    @ManyToOne
    @NotNull(message = "Type of card must not be empty")
    @JoinColumn(name = "card_type_id")
    private CardType cardType;
}
