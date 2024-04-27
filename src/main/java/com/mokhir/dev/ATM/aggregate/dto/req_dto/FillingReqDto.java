package com.mokhir.dev.ATM.aggregate.dto.req_dto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.Map;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FillingReqDto {
    @Pattern(regexp = "\\d{16}", message = "Card number must contain exactly 16 digits")
    @Positive(message = "card number must be positive value")
    private String cardNumber;
    @NotBlank(message = "PIN code must not be empty")
    @Pattern(regexp = "\\d{4}", message = "PIN code must be exactly 4 digits long")
    private String cardPin;
    @Pattern(regexp = "^(true|false)$", message = "Please enter 'true' or 'false'")
    private String chequeIsNeed;
    @NotNull
    @Min(5000)
    private String amount;
}
