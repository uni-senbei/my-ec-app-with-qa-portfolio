package com.example.my_test_app.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// この例外がスローされたときにHTTP 400 Bad Requestを返すように設定
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class CartLimitExceededException extends RuntimeException {

    public CartLimitExceededException(String message) {
        super(message);
    }
}