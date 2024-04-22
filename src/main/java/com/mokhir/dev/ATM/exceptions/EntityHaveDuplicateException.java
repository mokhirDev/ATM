package com.mokhir.dev.ATM.exceptions;

public class EntityHaveDuplicateException extends Exception{
    public EntityHaveDuplicateException(String message) {
        super(message);
    }

    public EntityHaveDuplicateException(Throwable cause) {
        super(cause);
    }
}
