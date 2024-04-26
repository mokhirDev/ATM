package com.mokhir.dev.ATM.aggregate.dto.req_dto;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.format.annotation.NumberFormat;

import java.io.Serial;
import java.io.Serializable;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CardReqDto implements Serializable {
    @Serial
    private static final long serialVersionUID = -612968067558184215L;
    @Positive(message = "ID must be a positive number")
    private Long id;

    @NotNull(message = "Please create a PIN code")
    @Size(min = 4, max = 4, message = "The PIN code must be 4 characters long")
    private String cardPin;

    @NotNull(message = "User have to id")
    private Long userId;

    @NotNull(message = "User have to id")
    private Long cardTypeId;
}
