package com.mokhir.dev.ATM.aggregate.entity;

import jakarta.persistence.*;
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
@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"passportNumber", "passportSeries"})
})
public class CardHolder implements Serializable {
    @Serial
    private static final long serialVersionUID = 7268802135164965989L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "The address must not be empty")
    private String address;

    @NotNull(message = "Date of birth must not be empty")
    @Past(message = "Date of birth must be in the past")
    private LocalDate birthDate;

    @NotBlank(message = "Email must not be empty")
    @Email(message = "Incorrect email format")
    @Column(unique = true)
    private String email;

    @NotBlank(message = "The name must not be empty")
    private String name;

    @NotBlank(message = "Last name must not be empty")
    private String lastName;

    @NotNull(message = "Passport number must not be empty")
    @Size(min = 7, max = 7, message = "Passport number must be exactly 7 numbers long")
    @Pattern(regexp = "\\d{7}", message = "PIN FL must contain only digits")
    private String passportNumber;

    @NotBlank(message = "The passport series must not be empty")
    @Pattern(regexp = "[a-zA-Z]{2}", message = "The passport series must contain exactly two letters")
    private String passportSeries;

    @NotBlank(message = "Phone number must have value")
    @Pattern(regexp = "^998(9[012345789]|6[125679]|7[01234569])[0-9]{7}$", message = "Not matched phone number")
    @Size(min = 12, max = 12, message = "Phone numbers must be 12 digits long")
    @Column(unique = true)
    private String phoneNumber;

    @NotBlank(message = "PIN FL must not be empty")
    @Size(min = 14, max = 14, message = "PIN FL must be exactly 14 digits long")
    @Pattern(regexp = "\\d{14}", message = "PIN FL must contain only digits")
    @Column(unique = true)
    private String pinFl;
}
