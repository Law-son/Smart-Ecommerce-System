package org.example.dto;

/**
 * Data Transfer Object for User operations.
 * Used for transferring user data between UI, services, and DAO layers.
 */
public class UserDTO {
    private String fullName;
    private String email;
    private String password;
    private String role;

    // Default constructor
    public UserDTO() {
    }

    // Constructor
    public UserDTO(String fullName, String email, String password, String role) {
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    // Getters and Setters
    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}

