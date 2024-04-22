package com.mokhir.dev.ATM.exceptions;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ErrorResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 2577005872252876067L;
    private int code;
    private String message;
    private String meta;
}
