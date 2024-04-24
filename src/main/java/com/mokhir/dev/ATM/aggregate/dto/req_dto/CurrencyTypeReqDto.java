package com.mokhir.dev.ATM.aggregate.dto.req_dto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CurrencyTypeReqDto {
    private Long id;
    @Size(min = 3, max = 3, message = "Currency type must be 3 length")
    @NotNull(message = "Currency type must not be empty")
    @Column(unique = true, nullable = false)
    @Pattern(regexp = "[a-zA-Z]{3}", message = "The string must contain exactly three digits")
    private String name;
}
