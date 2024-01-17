package com.example.api.exception;

public class CustomerNotFound extends RuntimeException {

    public CustomerNotFound(String message) {
        super(message);
    }
}