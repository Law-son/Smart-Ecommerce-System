package org.example.utils.validators;

import java.util.regex.Pattern;

/**
 * Validator for email addresses.
 * Follows Single Responsibility Principle - only handles email validation.
 */
public class EmailValidator {
    
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );
    
    /**
     * Validates email format.
     *
     * @param email Email address to validate
     * @return true if email format is valid, false otherwise
     */
    public boolean isValid(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }
}



