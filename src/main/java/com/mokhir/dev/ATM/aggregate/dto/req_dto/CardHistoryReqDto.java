package com.mokhir.dev.ATM.aggregate.dto.req_dto;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CardHistoryReqDto {
    private Long id;
    private Long idFrom;
    private Long idTo;
    private BigDecimal amount;
    private LocalDateTime date;
}
