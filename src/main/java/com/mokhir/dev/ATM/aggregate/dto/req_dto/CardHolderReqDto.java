package com.mokhir.dev.ATM.aggregate.dto.req_dto;

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
    @NotBlank(message = "The address must not be empty")
    private String address;

    @NotNull(message = "Date of birth must not be empty")
    private LocalDate birthDate;

    @NotBlank(message = "Email must not be empty")
    @Email(message = "Incorrect email format")
    private String email;

    @NotBlank(message = "The name must not be empty")
    private String name;

    @NotBlank(message = "Last name must not be empty")
    private String lastName;

    @NotNull(message = "Passport number must not be empty")
    private Integer passportNumber;

    @NotBlank(message = "The passport series must not be empty")
    private String passportSeries;

    @NotBlank(message = "Phone number must have value")
    @Pattern(regexp = "^998(9[012345789]|6[125679]|7[01234569])[0-9]{7}$", message = "Not matched phone number")
    private String phoneNumber;

    @NotBlank(message = "PIN FL must not be empty")
    @Size(min = 14, max = 14)
    private String pinFl;
}
