package com.mokhir.dev.ATM.exceptions;

public class NotEnoughFundsException extends Exception{
    public NotEnoughFundsException(String message) {
        super(message);
    }

    public NotEnoughFundsException(Throwable cause) {
        super(cause);
    }
}
