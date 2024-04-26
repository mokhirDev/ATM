package com.mokhir.dev.ATM.aggregate.dto.res_dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CashingResDto {
    private Long id;
    private String cardNumber;
    private String amount;
    private boolean chequeIsNeed;
    private Long cashingTypeId;
    private String message;
    private LocalDateTime transactionTime;
    private CurrencyTypeResDto currencyType;
    private Map<Integer, Integer> cashedNominals;
    private CardHolderResDto cardHolderResDto;
}
