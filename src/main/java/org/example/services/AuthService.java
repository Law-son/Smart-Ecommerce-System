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
     * @return true if signup successful, false otherwise
     */
    public boolean signup(UserDTO userDTO) {
        // Validate email format
        if (!emailValidator.isValid(userDTO.getEmail())) {
            System.err.println("Invalid email format: " + userDTO.getEmail());
            return false;
        }
        
        // Validate password strength
        if (!passwordValidator.isValid(userDTO.getPassword())) {
            System.err.println("Password does not meet strength requirements. Must be at least " + 
                             passwordValidator.getMinPasswordLength() + " characters with letters and numbers.");
            return false;
        }
        
        // Check if email already exists
        User existingUser = userDAO.getUserByEmail(userDTO.getEmail());
        if (existingUser != null) {
            System.err.println("Email already registered: " + userDTO.getEmail());
            return false;
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
        String hashedPassword = passwordHasher.hash(plainPassword);
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
