package com.mokhir.dev.ATM.aggregate.dto.res_dto;

import com.mokhir.dev.ATM.aggregate.dto.req_dto.CardHolderReqDto;
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
    private Long id;
    private Double balance;
    private String cardNumber;
    private CardHolderResDto cardHolder;
    private CardTypeResDto cardType;
}
