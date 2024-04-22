package com.mokhir.dev.ATM.aggregate.dto.res_dto;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CardHolderResDto implements Serializable {
    @Serial
    private static final long serialVersionUID = -5174810910105615590L;
    private Long id;
    private String name;
    private String lastName;
    private String phoneNumber;
}
