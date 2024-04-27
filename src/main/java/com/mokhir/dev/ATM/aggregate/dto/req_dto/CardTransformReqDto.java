package com.mokhir.dev.ATM.aggregate.dto.req_dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CardTransformReqDto {
    @Pattern(regexp = "\\d{16}", message = "Card number of sender must contain exactly 16 digits")
    @Positive(message = "card number must be positive value")
    private String cardFrom;
    @Pattern(regexp = "\\d{16}", message = "Card number of receiver must contain exactly 16 digits")
    @Positive(message = "card number must be positive value")
    private String cardTo;
    @NotBlank(message = "PIN code must not be empty")
    @Pattern(regexp = "\\d{4}", message = "PIN code must be exactly 4 digits long")
    private String cardPin;
    @Pattern(regexp = "^(true|false)$", message = "Please enter 'true' or 'false'")
    private String chequeIsNeed;
    @NotNull
    @Min(5000)
    private String amount;
}
