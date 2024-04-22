package com.mokhir.dev.ATM.aggregate.dto.req_dto;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.mokhir.dev.ATM.aggregate.entity.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CardReqDto implements Serializable {
    @Serial
    private static final long serialVersionUID = -612968067558184215L;

    @Positive(message = "Balance must have positive value")
    @Column(columnDefinition = "double default 0.0")
    private Double balance;

    @NotNull
    @Size(min = 3, max = 4, message = "CVC must be from 3 to 4 symbols")
    private String card_cvc;

    @NotNull
    @Column(unique = true, nullable = false)
    @JsonFormat(locale = "card_number")
    private String cardNumber;

    @NotNull
    @Size(min = 4, max = 4, message = "Пин-код должен быть длиной 4 символа")
    private String card_pin;

    @NotNull(message = "User have to value")
    @ManyToOne
    @JoinColumn(name = "cardholder_id")
    private CardHolder user;

    @NotNull(message = "Type of card must not be empty")
    @ManyToOne
    @JoinColumn(name = "card_type_id")
    private CardType cardType;
}
