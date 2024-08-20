package ru.mattakvshi.throttlingtesttask.helpers;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public class TooManyRequestsException extends Exception{
    public TooManyRequestsException(String message){
        super(message);
    }
}
