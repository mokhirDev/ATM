package com.mokhir.dev.ATM.aggregate.dto.req_dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CardTransformReqDto {
    private Long id;
    private Long idFrom;
    private Long idTo;
    private BigDecimal amount;
}
