package com.mokhir.dev.ATM.aggregate.dto.req_dto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CardHolderReqDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 8519021254226553355L;
    private Long id;
    private String address;
    private LocalDate birthDate;
    private String email;
    private String name;
    private String lastName;
    private String passportNumber;
    private String passportSeries;
    private String phoneNumber;
    private String pinFl;
}
