package com.mokhir.dev.ATM.aggregate.dto.res_dto;

import com.mokhir.dev.ATM.aggregate.entity.CashingType;
import com.mokhir.dev.ATM.aggregate.entity.CurrencyType;
import lombok.*;
import java.io.Serializable;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BankNoteResDto implements Serializable {
    private Long id;
    private String name;
    private Integer nominal;
    private Integer quantity;
    private CashingType cashingTypeId;
    private CurrencyType currencyTypeId;
}
