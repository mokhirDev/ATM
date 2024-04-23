package com.mokhir.dev.ATM.exceptions.controller;

import com.mokhir.dev.ATM.exceptions.ApiControllerException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import com.mokhir.dev.ATM.exceptions.DatabaseException;
import com.mokhir.dev.ATM.exceptions.ErrorResponse;
import com.mokhir.dev.ATM.exceptions.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Objects;

@RestControllerAdvice
public class ExceptionController {
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> on(NotFoundException ex) {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.CONFLICT.value(),
                "Didn't found",
                ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(DatabaseException.class)
    public ResponseEntity<ErrorResponse> on(DatabaseException ex) {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST.value(),
                "Some thing went wrong, issue in Database",
                ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(ApiControllerException.class)
    public ResponseEntity<ErrorResponse> on(ApiControllerException ex) {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST.value(),
                "Some thing wrong in api controller side",
                ex.getMessage());
        return ResponseEntity.status(errorResponse.getCode()).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ResponseEntity<ErrorResponse> on(MethodArgumentNotValidException ex) {
        String defaultMessage = Objects.requireNonNull(ex.getBindingResult().getFieldError()).getDefaultMessage();
        String fieldName = Objects.requireNonNull(ex.getBindingResult().getFieldError()).getField();
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Error in validation: %s: field name: %s".formatted(defaultMessage, fieldName), ex.getMessage());
        return ResponseEntity.status(errorResponse.getCode()).body(errorResponse);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        String errorMessage = ex.getMessage();
        String[] errorMessages = errorMessage.split(":");
        String errorInfo = errorMessages[2].split("]")[0];
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                "Duplicating exception: "+errorInfo,
                ex.getMessage());
        return ResponseEntity.status(errorResponse.getCode()).body(errorResponse);
    }
}
