package org.example.services;

import org.example.dao.UserDAO;
import org.example.dto.UserDTO;
import org.example.models.User;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Pattern;

/**
 * Authentication service handling user signup, login, and password management.
 * Handles password hashing, validation, and role routing logic.
 */
public class AuthService {
    private final UserDAO userDAO;
    
    // Email validation pattern
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );
    
    // Password strength requirements
    private static final int MIN_PASSWORD_LENGTH = 8;
    
    public AuthService() {
        this.userDAO = new UserDAO();
    }
    
    /**
     * Hashes a password using SHA-256 algorithm.
     *
     * @param password Plain text password
     * @return Hashed password as hexadecimal string
     */
    public String hashPassword(String password) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
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
            System.err.println("Error hashing password: SHA-256 algorithm not available");
            e.printStackTrace();
            throw new RuntimeException("Password hashing failed", e);
        }
    }
    
    /**
     * Validates email format.
     *
     * @param email Email address to validate
     * @return true if email format is valid, false otherwise
     */
    public boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }
    
    /**
     * Validates password strength.
     *
     * @param password Password to validate
     * @return true if password meets strength requirements, false otherwise
     */
    public boolean isValidPassword(String password) {
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
     * Signs up a new user.
     * Validates email format and password strength before creating the user.
     *
     * @param userDTO User data transfer object with plain password
     * @return true if signup successful, false otherwise
     */
    public boolean signup(UserDTO userDTO) {
        // Validate email format
        if (!isValidEmail(userDTO.getEmail())) {
            System.err.println("Invalid email format: " + userDTO.getEmail());
            return false;
        }
        
        // Validate password strength
        if (!isValidPassword(userDTO.getPassword())) {
            System.err.println("Password does not meet strength requirements. Must be at least " + 
                             MIN_PASSWORD_LENGTH + " characters with letters and numbers.");
            return false;
        }
        
        // Check if email already exists
        User existingUser = userDAO.getUserByEmail(userDTO.getEmail());
        if (existingUser != null) {
            System.err.println("Email already registered: " + userDTO.getEmail());
            return false;
        }
        
        // Hash password before storing
        String hashedPassword = hashPassword(userDTO.getPassword());
        UserDTO hashedUserDTO = new UserDTO();
        hashedUserDTO.setFullName(userDTO.getFullName());
        hashedUserDTO.setEmail(userDTO.getEmail());
        hashedUserDTO.setPassword(hashedPassword); // Store hashed password
        hashedUserDTO.setRole(userDTO.getRole() != null ? userDTO.getRole() : "CUSTOMER");
        
        // Create user via DAO
        boolean success = userDAO.createUser(hashedUserDTO);
        if (success) {
            System.out.println("User signed up successfully: " + userDTO.getEmail());
        } else {
            System.err.println("Failed to create user: " + userDTO.getEmail());
        }
        
        return success;
    }
    
    /**
     * Validates user login credentials.
     *
     * @param email User email
     * @param plainPassword Plain text password
     * @return User object if login successful, null otherwise
     */
    public User login(String email, String plainPassword) {
        if (email == null || email.isEmpty() || plainPassword == null || plainPassword.isEmpty()) {
            System.err.println("Email and password are required for login");
            return null;
        }
        
        // Get user from database
        User user = userDAO.getUserByEmail(email);
        if (user == null) {
            System.err.println("User not found: " + email);
            return null;
        }
        
        // Hash the provided password and compare with stored hash
        String hashedPassword = hashPassword(plainPassword);
        if (hashedPassword.equals(user.getPasswordHash())) {
            System.out.println("Login successful for user: " + email);
            return user;
        } else {
            System.err.println("Invalid password for user: " + email);
            return null;
        }
    }
    
    /**
     * Gets user role for routing logic.
     *
     * @param email User email
     * @return User role (ADMIN or CUSTOMER), null if user not found
     */
    public String getUserRole(String email) {
        User user = userDAO.getUserByEmail(email);
        if (user != null) {
            return user.getRole();
        }
        return null;
    }
    
    /**
     * Updates user information.
     * Hashes password if provided.
     *
     * @param userId User ID
     * @param userDTO Updated user data
     * @return true if update successful, false otherwise
     */
    public boolean updateUser(int userId, UserDTO userDTO) {
        // Validate email if provided
        if (userDTO.getEmail() != null && !isValidEmail(userDTO.getEmail())) {
            System.err.println("Invalid email format: " + userDTO.getEmail());
            return false;
        }
        
        // Hash password if provided
        if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()) {
            if (!isValidPassword(userDTO.getPassword())) {
                System.err.println("Password does not meet strength requirements");
                return false;
            }
            String hashedPassword = hashPassword(userDTO.getPassword());
            userDTO.setPassword(hashedPassword);
        }
        
        return userDAO.updateUser(userId, userDTO);
    }
}

