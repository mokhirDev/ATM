package com.mokhir.dev.ATM.aggregate.dto.req_dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CurrencyTypeReqDto {
    private Long id;
    @NotNull(message = "Currency type must not be empty")
    @Size(min = 3, max = 3)
    private String name;
}
