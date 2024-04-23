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

    @NotNull(message = "Please create a PIN code")
    @Size(min = 4, max = 4, message = "The PIN code must be 4 characters long")
    private Integer cardPin;

    @NotNull(message = "User have to id")
    @NumberFormat
    private Long userId;

    @NotNull(message = "User have to id")
    @NumberFormat
    private Long cardTypeId;
}
