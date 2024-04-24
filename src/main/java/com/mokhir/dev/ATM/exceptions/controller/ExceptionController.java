package com.mokhir.dev.ATM.exceptions.controller;

import com.mokhir.dev.ATM.exceptions.ApiControllerException;
import com.mokhir.dev.ATM.service.CardService;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.UnexpectedTypeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import com.mokhir.dev.ATM.exceptions.DatabaseException;
import com.mokhir.dev.ATM.exceptions.ErrorResponse;
import com.mokhir.dev.ATM.exceptions.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Objects;

@RestControllerAdvice
public class ExceptionController {
    private static final Logger LOG = LoggerFactory.getLogger(CardService.class);

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> on(NotFoundException ex) {
        LOG.error("NotFoundException: {} ", ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.CONFLICT.value(),
                "Didn't found",
                ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(DatabaseException.class)
    public ResponseEntity<ErrorResponse> on(DatabaseException ex) {
        LOG.error("DatabaseException: {} ", ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST.value(),
                "Some thing went wrong, issue in Database",
                ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(ApiControllerException.class)
    public ResponseEntity<ErrorResponse> on(ApiControllerException ex) {
        LOG.error("ApiControllerException: {} ", ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST.value(),
                "Some thing wrong in api controller side",
                ex.getMessage());
        return ResponseEntity.status(errorResponse.getCode()).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ResponseEntity<ErrorResponse> on(MethodArgumentNotValidException ex) {
        LOG.error("MethodArgumentNotValidException: {} ", ex.getMessage());
        String defaultMessage = Objects.requireNonNull(ex.getBindingResult().getFieldError()).getDefaultMessage();
        String fieldName = Objects.requireNonNull(ex.getBindingResult().getFieldError()).getField();
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Error in validation: %s: field name: %s".formatted(defaultMessage, fieldName), ex.getMessage());
        return ResponseEntity.status(errorResponse.getCode()).body(errorResponse);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> on(DataIntegrityViolationException ex) {
        LOG.error("DataIntegrityViolationException: {} ", ex.getMessage());
        String errorMessage = ex.getMessage();
        String[] errorMessages = errorMessage.split(":");
        String errorInfo = errorMessages[2].split("]")[0];
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                "Duplicating exception: " + errorInfo,
                ex.getMessage());
        return ResponseEntity.status(errorResponse.getCode()).body(errorResponse);
    }

    @ExceptionHandler(UnexpectedTypeException.class)
    public ResponseEntity<ErrorResponse> on(UnexpectedTypeException ex) {
        LOG.error("UnexpectedTypeException: {} ", ex.getMessage());
        String errorMessage = ex.getMessage();
        String[] errorMessages = errorMessage.split("\\.");
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                "Validation error: " + errorMessages[errorMessages.length - 1],
                ex.getMessage());
        return ResponseEntity.status(errorResponse.getCode()).body(errorResponse);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> on(ConstraintViolationException ex) {
        LOG.error("ConstraintViolationException: {} ", ex.getMessage());
        StringBuilder errorMessage = new StringBuilder();
        ex.getConstraintViolations().forEach(violation ->
                errorMessage.append(violation.getMessage()).append("; "));
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                errorMessage.toString(),
                ex.getMessage());
        return ResponseEntity.status(errorResponse.getCode()).body(errorResponse);
    }

    @ExceptionHandler(MissingPathVariableException.class)
    public ResponseEntity<ErrorResponse> handleMissingPathVariable(MissingPathVariableException ex) {
        LOG.error("MissingPathVariableException: {} ", ex.getMessage());
        String errorMessage = "Required URI template variable '" + ex.getVariableName() + "' is not present";
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                errorMessage,
                ex.getMessage());
        return ResponseEntity.status(errorResponse.getCode()).body(errorResponse);
    }
}
