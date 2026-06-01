package com.punam.eventledger.exception;

import org.springframework.http.*;
import org.springframework.web.bind.*;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(
            EventNotFoundException.class)
    public ResponseEntity<Map<String,String>>
    handleNotFound(
            EventNotFoundException ex) {

        Map<String,String> response =
                new HashMap<>();

        response.put(
                "message",
                ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(response);
    }

    @ExceptionHandler(
            MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String,String>>
    handleValidation(
            MethodArgumentNotValidException ex) {

        Map<String,String> response =
                new HashMap<>();

        ex.getBindingResult()
                .getFieldErrors()
                .forEach(error ->
                        response.put(
                                error.getField(),
                                error.getDefaultMessage()
                        ));

        return ResponseEntity
                .badRequest()
                .body(response);
    }
}
