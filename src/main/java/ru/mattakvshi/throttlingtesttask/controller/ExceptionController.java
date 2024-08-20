package ru.mattakvshi.throttlingtesttask.controller;


import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.mattakvshi.throttlingtesttask.helpers.TooManyRequestsException;

@RestControllerAdvice
public class ExceptionController {

    @ExceptionHandler(TooManyRequestsException.class)
    public ResponseEntity<String> handleTooManyRequestsException(TooManyRequestsException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.valueOf(502));
    }
}
