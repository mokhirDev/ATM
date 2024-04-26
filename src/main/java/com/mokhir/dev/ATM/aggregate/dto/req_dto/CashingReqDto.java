package com.mokhir.dev.ATM.aggregate.dto.req_dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CashingReqDto {
    @Pattern(regexp = "\\d{16}", message = "Card number must contain exactly 16 digits")
    @Positive(message = "card number must be positive value")
    private String cardNumber;
    @NotBlank(message = "PIN code must not be empty")
    @Pattern(regexp = "\\d{4}", message = "PIN code must be exactly 4 digits long")
    private String cardPin;
    @Positive(message = "Amount must be a positive number")
    private String amount;
    @Pattern(regexp = "^(true|false)$", message = "Please enter 'true' or 'false'")
    private String chequeIsNeed;
    @Positive(message = "cashing type id must be positive value")
    private String cashingTypeId;
}
