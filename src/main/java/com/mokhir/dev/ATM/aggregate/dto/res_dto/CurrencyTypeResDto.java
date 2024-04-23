package com.mokhir.dev.ATM.aggregate.dto.res_dto;

import lombok.*;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CurrencyTypeResDto {
    private Long id;
    private String name;
}
