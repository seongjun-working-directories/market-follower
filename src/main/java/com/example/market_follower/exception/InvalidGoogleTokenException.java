package com.example.market_follower.exception;

import lombok.Getter;

@Getter
public class InvalidGoogleTokenException extends RuntimeException {
    public InvalidGoogleTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}