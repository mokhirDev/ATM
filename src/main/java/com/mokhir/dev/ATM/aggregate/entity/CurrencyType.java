package com.mokhir.dev.ATM.aggregate.entity;

import jakarta.persistence.*;
import lombok.*;
import java.io.*;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class CurrencyType implements Serializable {
    @Serial
    private static final long serialVersionUID = -8270487372654413151L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
}
