package com.mokhir.dev.ATM.aggregate.entity;

import jakarta.persistence.*;
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
public class CardHolder implements Serializable {
    @Serial
    private static final long serialVersionUID = 7268802135164965989L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String address;
    private LocalDate birthDate;
    @Column(unique = true)
    private String email;
    private String name;
    private String lastName;
    private Long passportNumber;
    private String passportSeries;
    @Column(unique = true)
    private String passportInfo;
    @Column(unique = true)
    private String phoneNumber;
    @Column(unique = true)
    private String pinFl;
}
