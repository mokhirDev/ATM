package com.mokhir.dev.ATM.aggregate.dto.res_dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.mokhir.dev.ATM.aggregate.entity.Card;
import lombok.*;

import java.time.LocalDateTime;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CardTransformResDto {
    private Long id;
    private Card cardNumberSender;
    private String cardNumberReceiver;
    private String amount;
    private String commission;
    private boolean chequeIsNeed;
    private String message;
    private LocalDateTime transactionTime;
    private CardHolderResDto cardHolderResDto;
}
