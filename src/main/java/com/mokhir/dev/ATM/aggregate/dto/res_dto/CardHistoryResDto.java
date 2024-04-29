package com.mokhir.dev.ATM.aggregate.dto.res_dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class CardHistoryResDto {
    private Long id;
    private BigDecimal amount;
    private BigDecimal commission;
    private LocalDateTime date;
    private CardResDto fromCard;
    private CardResDto toCard;
}
