package org.example.utils.validators;

/**
 * Validator for password strength.
 * Follows Single Responsibility Principle - only handles password validation.
 */
public class PasswordValidator {
    
    private static final int MIN_PASSWORD_LENGTH = 8;
    
    /**
     * Validates password strength.
     *
     * @param password Password to validate
     * @return true if password meets strength requirements, false otherwise
     */
    public boolean isValid(String password) {
        if (password == null || password.isEmpty()) {
            return false;
        }
        
        // Check minimum length
        if (password.length() < MIN_PASSWORD_LENGTH) {
            return false;
        }
        
        // Check for at least one letter and one number
        boolean hasLetter = false;
        boolean hasNumber = false;
        
        for (char c : password.toCharArray()) {
            if (Character.isLetter(c)) {
                hasLetter = true;
            } else if (Character.isDigit(c)) {
                hasNumber = true;
            }
        }
        
        return hasLetter && hasNumber;
    }
    
    /**
     * Gets the minimum password length requirement.
     *
     * @return Minimum password length
     */
    public int getMinPasswordLength() {
        return MIN_PASSWORD_LENGTH;
    }
}




