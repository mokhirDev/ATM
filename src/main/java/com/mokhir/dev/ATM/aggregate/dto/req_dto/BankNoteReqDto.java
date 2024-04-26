package com.mokhir.dev.ATM.aggregate.dto.req_dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BankNoteReqDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 7490249568159776687L;
    @NotNull
    @NotBlank
    private String name;
    @NotNull
    @NotBlank
    private Integer nominal;
    @NotNull
    @NotBlank
    private Integer quantity;
    @NotNull
    @NotBlank
    private Long cashingType;
    @NotNull
    @NotBlank
    private Long currencyType;
}
