package com.tourism.exception;

import lombok.extern.slf4j.Slf4j;
import org.apache.jena.query.QueryException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(QueryException.class)
    public ResponseEntity<ErrorResponse> handleQueryException(QueryException ex) {
        log.error("SPARQL Query Failed", ex);
        ErrorResponse error = ErrorResponse.builder()
            .message("SPARQL Query Execution Failed")
            .status(HttpStatus.BAD_REQUEST.value())
            .detail(ex.getMessage())
            .timestamp(LocalDateTime.now())
            .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex) {
        log.error("Unexpected runtime error", ex);
        ErrorResponse error = ErrorResponse.builder()
            .message("Internal Server Error")
            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .detail(ex.getMessage())
            .timestamp(LocalDateTime.now())
            .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

}
