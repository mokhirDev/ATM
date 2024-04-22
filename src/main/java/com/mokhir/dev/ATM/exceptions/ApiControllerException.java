package com.mokhir.dev.ATM.exceptions;

public class ApiControllerException extends Exception{
    public ApiControllerException(String message) {
        super(message);
    }

    public ApiControllerException(Throwable cause) {
        super(cause);
    }
}
