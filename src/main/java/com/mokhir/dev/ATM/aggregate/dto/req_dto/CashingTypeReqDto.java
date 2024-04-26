package com.mokhir.dev.ATM.aggregate.dto.req_dto;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CashingTypeReqDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 8369292371604748963L;
    private String name;
}
