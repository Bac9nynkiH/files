package com.challenge.files.controller;

import com.challenge.files.exception.NotFoundException;
import com.challenge.files.exception.ServerException;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;


@Log4j2
@ControllerAdvice
public class ExceptionControllerAdvice {
    @ExceptionHandler(ServerException.class)
    public ResponseEntity<?> handleServerException(ServerException e) {
        log.error("[handleServerException] " + e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<?> handleNotFoundException(NotFoundException e) {
        log.error("[handleNotFoundException] " + e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }

    @ExceptionHandler(Throwable.class)
    public ResponseEntity<?> handleThrowableException(Throwable t) {
        log.error("[handleThrowableException] " + t.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}
