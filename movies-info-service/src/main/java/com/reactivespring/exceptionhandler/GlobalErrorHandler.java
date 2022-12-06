package com.reactivespring.exceptionhandler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ResponseStatusException;

import java.util.stream.Collectors;

@ControllerAdvice
@Slf4j
public class GlobalErrorHandler {

    // This helps during API Req.Body/Params validation in Controllers
    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<String> handlerRequestBodyError(WebExchangeBindException ex) {
        log.error("Exception caught in handlerRequestBodyError() : {} " + ex.getMessage() + ex);
        var error = ex.getBindingResult()
                .getAllErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .sorted()
                .collect(Collectors.joining(","));
        log.error("Error is : {} ", error);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(error);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<String> handleResponseStatusException(ResponseStatusException ex) {
        int statusCode = ex.getRawStatusCode();
        String errorBody = ex.getMessage();
        log.error("Inside GlobalExceptionHandler for ResponseStatusException code : {}, errorBody : {}", statusCode, errorBody);

        return ResponseEntity.status(statusCode)
                .body(errorBody);
    }
}
