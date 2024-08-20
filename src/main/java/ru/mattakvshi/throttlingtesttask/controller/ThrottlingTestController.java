package ru.mattakvshi.throttlingtesttask.controller;

import org.springframework.http.HttpStatus;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import ru.mattakvshi.throttlingtesttask.helpers.RateLimited;


@RestController
@RequestMapping("/api")
public class ThrottlingTestController {

    @RateLimited
    @GetMapping("/throttling/test")
    public ResponseEntity<String> throttlingTest() {
        return new ResponseEntity<>("", HttpStatus.valueOf(200));
    }

}
