package org.example.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utility class for password hashing operations.
 * Follows Single Responsibility Principle - only handles password hashing.
 */
public class PasswordHasher {
    
    private static final String HASH_ALGORITHM = "SHA-256";
    
    /**
     * Hashes a password using SHA-256 algorithm.
     *
     * @param password Plain text password
     * @return Hashed password as hexadecimal string
     * @throws IllegalArgumentException if password is null or empty
     * @throws RuntimeException if hashing algorithm is not available
     */
    public String hash(String password) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        
        try {
            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
            byte[] hashBytes = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            
            // Convert bytes to hexadecimal string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Password hashing failed: " + HASH_ALGORITHM + " algorithm not available", e);
        }
    }
}




