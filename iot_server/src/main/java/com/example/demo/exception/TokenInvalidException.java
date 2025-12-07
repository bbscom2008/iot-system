package com.example.demo.exception;

/**
 * Token 无效异常
 */
public class TokenInvalidException extends RuntimeException {
    
    public TokenInvalidException(String message) {
        super(message);
    }
    
    public TokenInvalidException(String message, Throwable cause) {
        super(message, cause);
    }
}

