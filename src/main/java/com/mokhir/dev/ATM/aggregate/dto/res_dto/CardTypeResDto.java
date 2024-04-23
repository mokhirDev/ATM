package com.mokhir.dev.ATM.aggregate.dto.res_dto;

import com.mokhir.dev.ATM.aggregate.entity.CurrencyType;
import com.mokhir.dev.ATM.aggregate.enums.CardTypeName;
import lombok.*;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CardTypeResDto {
    private Long id;
    private CardTypeName name;
    private Integer number;
    private Integer expirationYear;
    private CurrencyType currencyType;
}
