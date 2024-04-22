package com.mokhir.dev.ATM.exceptions;

public class CurrentUserNotOwnCurrentEntityException extends Exception{
    public CurrentUserNotOwnCurrentEntityException(String message) {
        super(message);
    }

    public CurrentUserNotOwnCurrentEntityException(Throwable cause) {
        super(cause);
    }
}
