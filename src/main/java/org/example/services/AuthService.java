package org.example.services;

import org.example.dao.UserDAO;
import org.example.dto.UserDTO;
import org.example.models.User;
import org.example.utils.PasswordHasher;
import org.example.utils.validators.EmailValidator;
import org.example.utils.validators.PasswordValidator;

/**
 * Authentication service handling user signup, login, and password management.
 * Follows Single Responsibility Principle by delegating validation and hashing to dedicated classes.
 */
public class AuthService {
    private final UserDAO userDAO;
    private final PasswordHasher passwordHasher;
    private final EmailValidator emailValidator;
    private final PasswordValidator passwordValidator;
    
    public AuthService() {
        this.userDAO = new UserDAO();
        this.passwordHasher = new PasswordHasher();
        this.emailValidator = new EmailValidator();
        this.passwordValidator = new PasswordValidator();
    }
    
    /**
     * Signs up a new user.
     * Validates email format and password strength before creating the user.
     *
     * @param userDTO User data transfer object with plain password
     * @return SignupResult with success status and specific error message if failed
     */
    public SignupResult signup(UserDTO userDTO) {
        // Validate email format
        if (!emailValidator.isValid(userDTO.getEmail())) {
            return SignupResult.failure("Invalid email format. Please enter a valid email address.");
        }
        
        // Validate password strength
        if (!passwordValidator.isValid(userDTO.getPassword())) {
            int minLength = passwordValidator.getMinPasswordLength();
            return SignupResult.failure("Password does not meet requirements. Password must be at least " + 
                             minLength + " characters long and contain both letters and numbers.");
        }
        
        // Check if email already exists
        User existingUser = userDAO.getUserByEmail(userDTO.getEmail());
        if (existingUser != null) {
            return SignupResult.failure("An account with this email already exists. Please use a different email or try logging in.");
        }
        
        // Hash password before storing
        String hashedPassword = passwordHasher.hash(userDTO.getPassword());
        UserDTO hashedUserDTO = new UserDTO();
        hashedUserDTO.setFullName(userDTO.getFullName());
        hashedUserDTO.setEmail(userDTO.getEmail());
        hashedUserDTO.setPassword(hashedPassword);
        hashedUserDTO.setRole(userDTO.getRole() != null ? userDTO.getRole() : "CUSTOMER");
        
        // Create user via DAO
        boolean success = userDAO.createUser(hashedUserDTO);
        if (success) {
            System.out.println("User signed up successfully: " + userDTO.getEmail());
            return SignupResult.success();
        } else {
            return SignupResult.failure("Failed to create account. Database error occurred. Please try again.");
        }
    }
    
    /**
     * Validates user login credentials.
     *
     * @param email User email
     * @param plainPassword Plain text password
     * @return AuthResult with success status and specific error message if failed
     */
    public AuthResult login(String email, String plainPassword) {
        // Validate email is provided
        if (email == null || email.trim().isEmpty()) {
            return AuthResult.failure("Email is required");
        }
        
        // Validate password is provided
        if (plainPassword == null || plainPassword.isEmpty()) {
            return AuthResult.failure("Password is required");
        }
        
        // Validate email format
        if (!emailValidator.isValid(email.trim())) {
            return AuthResult.failure("Invalid email format. Please enter a valid email address.");
        }
        
        // Get user from database
        User user = userDAO.getUserByEmail(email.trim());
        if (user == null) {
            return AuthResult.failure("No account found with this email address.");
        }
        
        // Hash the provided password and compare with stored hash
        String hashedPassword = passwordHasher.hash(plainPassword);
        if (hashedPassword.equals(user.getPasswordHash())) {
            System.out.println("Login successful for user: " + email);
            return AuthResult.success(user);
        } else {
            return AuthResult.failure("Incorrect password. Please check your password and try again.");
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
        if (userDTO.getEmail() != null && !emailValidator.isValid(userDTO.getEmail())) {
            System.err.println("Invalid email format: " + userDTO.getEmail());
            return false;
        }
        
        // Hash password if provided
        if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()) {
            if (!passwordValidator.isValid(userDTO.getPassword())) {
                System.err.println("Password does not meet strength requirements");
                return false;
            }
            String hashedPassword = passwordHasher.hash(userDTO.getPassword());
            userDTO.setPassword(hashedPassword);
        }
        
        return userDAO.updateUser(userId, userDTO);
    }
    
    /**
     * Logs out the current user by clearing session cache.
     * This method should be called when user logs out to ensure session data is cleared.
     *
     * @return true if logout successful (always returns true as it's just clearing cache)
     */
    public boolean logout() {
        // Session clearing is handled by SessionManager
        // This method exists for service layer consistency
        System.out.println("User logged out successfully");
        return true;
    }
}
