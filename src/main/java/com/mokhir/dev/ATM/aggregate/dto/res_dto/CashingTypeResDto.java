package com.mokhir.dev.ATM.aggregate.dto.res_dto;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CashingTypeResDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 7952966791586034664L;
    private Long id;
    private String name;
}
