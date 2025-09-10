package com.example.market_follower.exception;

import lombok.Getter;

@Getter
public class DeactivatedAccountException extends RuntimeException {
    public DeactivatedAccountException(String message) {
        super(message);
    }
}