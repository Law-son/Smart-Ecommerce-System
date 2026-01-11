package org.example.utils;

import org.example.models.User;

/**
 * Singleton utility class for managing user session.
 * Stores the currently logged-in user.
 */
public class SessionManager {
    private static SessionManager instance;
    private User currentUser;
    
    private SessionManager() {
        // Private constructor for singleton
    }
    
    /**
     * Gets the singleton instance of SessionManager.
     *
     * @return SessionManager instance
     */
    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }
    
    /**
     * Sets the current logged-in user.
     *
     * @param user User object
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }
    
    /**
     * Gets the current logged-in user.
     *
     * @return User object, or null if not logged in
     */
    public User getCurrentUser() {
        return currentUser;
    }
    
    /**
     * Checks if a user is currently logged in.
     *
     * @return true if user is logged in, false otherwise
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }
    
    /**
     * Checks if the current user is an admin.
     *
     * @return true if user is admin, false otherwise
     */
    public boolean isAdmin() {
        return currentUser != null && "ADMIN".equalsIgnoreCase(currentUser.getRole());
    }
    
    /**
     * Clears the current session (logout).
     */
    public void clearSession() {
        this.currentUser = null;
    }
    
    /**
     * Gets the current user's ID.
     *
     * @return User ID, or -1 if not logged in
     */
    public int getCurrentUserId() {
        return currentUser != null ? currentUser.getUserId() : -1;
    }
}




