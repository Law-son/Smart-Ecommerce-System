package org.example.services;

import org.example.models.User;

/**
 * Result object for authentication operations.
 * Contains success status, user (if successful), and specific error message.
 */
public class AuthResult {
    private final boolean success;
    private final User user;
    private final String errorMessage;
    
    private AuthResult(boolean success, User user, String errorMessage) {
        this.success = success;
        this.user = user;
        this.errorMessage = errorMessage;
    }
    
    public static AuthResult success(User user) {
        return new AuthResult(true, user, null);
    }
    
    public static AuthResult failure(String errorMessage) {
        return new AuthResult(false, null, errorMessage);
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public User getUser() {
        return user;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
}

