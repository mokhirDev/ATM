package com.mokhir.dev.ATM.aggregate.dto.req_dto;

import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.ManyToAny;
import org.hibernate.validator.constraints.Length;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CardTypeReqDto {

    @NotNull(message = "Card type name must not be empty")
    @Length(min = 3, max = 7, message = "Card type name must be minimum 3 and maximum 7 length")
    private String name;

    @NotNull(message = "Card number must not be empty")
    @Size(min = 6, max = 6, message = "Card number must be only 6 digits")
    @Pattern(regexp = "\\d{6}", message = "Card number must contain only digits")
    private String number;

    @NotNull(message = "Currency type id must not be empty")
    private Long currencyTypeId;

    @NotNull(message = "Expiration must not be empty")
    @Min(value = 1)
    @Max(value = 7)
    private Integer expirationYear;
}
