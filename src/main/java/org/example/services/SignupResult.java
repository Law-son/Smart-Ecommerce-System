package org.example.services;

/**
 * Result object for signup operations.
 * Contains success status and specific error message.
 */
public class SignupResult {
    private final boolean success;
    private final String errorMessage;
    
    private SignupResult(boolean success, String errorMessage) {
        this.success = success;
        this.errorMessage = errorMessage;
    }
    
    public static SignupResult success() {
        return new SignupResult(true, null);
    }
    
    public static SignupResult failure(String errorMessage) {
        return new SignupResult(false, errorMessage);
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
}

