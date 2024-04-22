package com.mokhir.dev.ATM.aggregate.dto.res_dto;

import com.mokhir.dev.ATM.aggregate.entity.CardHolder;
import com.mokhir.dev.ATM.aggregate.entity.CardType;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CardResDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 5450761978182490663L;
    private Double balance;
    private String cardNumber;
    private CardHolder user;
    private CardType cardType;
}
