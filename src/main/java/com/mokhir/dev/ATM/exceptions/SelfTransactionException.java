package com.mokhir.dev.ATM.exceptions;

public class SelfTransactionException extends Exception{
    public SelfTransactionException(String message) {
        super(message);
    }
}
