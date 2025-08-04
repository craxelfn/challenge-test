package com.marketingconfort.challenge.exception;

public class ChallengeValidationException extends RuntimeException {
    
    private final String errorCode;
    private final String field;
    
    public ChallengeValidationException(String message, String errorCode, String field) {
        super(message);
        this.errorCode = errorCode;
        this.field = field;
    }
    
    public ChallengeValidationException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.field = null;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public String getField() {
        return field;
    }
} 