package org.example.dao;

import org.example.configs.DatabaseConfig;
import org.example.dto.UserDTO;
import org.example.models.User;

import java.sql.*;
import java.time.LocalDateTime;

/**
 * Data Access Object for User operations.
 * Handles all database interactions for User entities.
 */
public class UserDAO {

    /**
     * Retrieves a user by email address.
     *
     * @param email The email address to search for
     * @return User object if found, null otherwise
     */
    public User getUserByEmail(String email) {
        String sql = "SELECT user_id, full_name, email, password_hash, role, created_at FROM users WHERE email = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, email);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setUserId(rs.getInt("user_id"));
                    user.setFullName(rs.getString("full_name"));
                    user.setEmail(rs.getString("email"));
                    user.setPasswordHash(rs.getString("password_hash"));
                    user.setRole(rs.getString("role"));
                    Timestamp createdAt = rs.getTimestamp("created_at");
                    if (createdAt != null) {
                        user.setCreatedAt(createdAt.toLocalDateTime());
                    }
                    return user;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving user by email: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }

    /**
     * Creates a new user in the database.
     * Note: Password should be hashed in the service layer before calling this method.
     *
     * @param userDTO The user data transfer object
     * @return true if user was created successfully, false otherwise
     */
    public boolean createUser(UserDTO userDTO) {
        String sql = "INSERT INTO users (full_name, email, password_hash, role) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, userDTO.getFullName());
            pstmt.setString(2, userDTO.getEmail());
            // Password should be hashed before calling this method
            pstmt.setString(3, userDTO.getPassword());
            pstmt.setString(4, userDTO.getRole());
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error creating user: " + e.getMessage());
            if (e.getSQLState().equals("23505")) { // Unique constraint violation
                System.err.println("Email already exists: " + userDTO.getEmail());
            }
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Deletes a user by user ID.
     *
     * @param userId The ID of the user to delete
     * @return true if user was deleted successfully, false otherwise
     */
    public boolean deleteUser(int userId) {
        String sql = "DELETE FROM users WHERE user_id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error deleting user: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Updates an existing user.
     * Note: Password should be hashed in the service layer before calling this method.
     *
     * @param userId The ID of the user to update
     * @param userDTO The updated user data
     * @return true if user was updated successfully, false otherwise
     */
    public boolean updateUser(int userId, UserDTO userDTO) {
        String sql = "UPDATE users SET full_name = ?, email = ?, password_hash = ?, role = ? WHERE user_id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, userDTO.getFullName());
            pstmt.setString(2, userDTO.getEmail());
            // Password should be hashed before calling this method
            pstmt.setString(3, userDTO.getPassword());
            pstmt.setString(4, userDTO.getRole());
            pstmt.setInt(5, userId);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating user: " + e.getMessage());
            if (e.getSQLState().equals("23505")) { // Unique constraint violation
                System.err.println("Email already exists: " + userDTO.getEmail());
            }
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Validates user login credentials.
     * Note: Password should be hashed in the service layer before calling this method.
     *
     * @param email The user's email
     * @param passwordHash The hashed password to validate
     * @return true if credentials are valid, false otherwise
     */
    public boolean validateLogin(String email, String passwordHash) {
        String sql = "SELECT password_hash FROM users WHERE email = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, email);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password_hash");
                    return storedHash != null && storedHash.equals(passwordHash);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error validating login: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
}

