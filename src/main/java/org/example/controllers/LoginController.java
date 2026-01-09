package org.example.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.Scene;
import org.example.models.User;
import org.example.services.AuthService;
import org.example.utils.NavigationHelper;
import org.example.utils.SessionManager;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for the Login screen.
 * Handles user authentication and navigation to Dashboard.
 */
public class LoginController implements Initializable {
    
    @FXML
    private TextField emailField;
    
    @FXML
    private PasswordField passwordField;
    
    @FXML
    private Button loginButton;
    
    @FXML
    private Button signupButton;
    
    @FXML
    private Label errorLabel;
    
    private AuthService authService;
    private SessionManager sessionManager;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.authService = new AuthService();
        this.sessionManager = SessionManager.getInstance();
        
        // Clear any previous session
        sessionManager.clearSession();
        
        // Set up event handlers
        loginButton.setOnAction(e -> handleLogin());
        signupButton.setOnAction(e -> handleSignup());
        
        // Clear error label initially
        errorLabel.setText("");
    }
    
    /**
     * Handles login button click.
     */
    private void handleLogin() {
        try {
            String email = emailField.getText().trim();
            String password = passwordField.getText();
            
            // Clear previous errors
            errorLabel.setText("");
            
            // Validate input
            if (email.isEmpty() || password.isEmpty()) {
                errorLabel.setText("Please enter both email and password");
                return;
            }
            
            // Attempt login via AuthService
            User user = authService.login(email, password);
            
            if (user != null) {
                // Store user in session (session caching)
                sessionManager.setCurrentUser(user);
                
                // Log successful login with user details
                System.out.println("User logged in: " + user.getFullName() + " (Role: " + user.getRole() + ")");
                
                // Navigate to Dashboard
                Scene scene = loginButton.getScene();
                if (scene != null) {
                    boolean navigated = NavigationHelper.navigateTo("Dashboard.fxml", NavigationHelper.getStage(scene));
                    if (!navigated) {
                        errorLabel.setText("Failed to navigate to dashboard. Please try again.");
                    }
                } else {
                    errorLabel.setText("Navigation error. Please try again.");
                }
            } else {
                errorLabel.setText("Invalid email or password. Please try again.");
            }
        } catch (Exception e) {
            System.err.println("Error during login: " + e.getMessage());
            e.printStackTrace();
            errorLabel.setText("An error occurred during login. Please try again.");
        }
    }
    
    /**
     * Handles signup button click.
     * TODO: Implement signup screen navigation
     */
    private void handleSignup() {
        errorLabel.setText("Signup functionality not yet implemented");
        // Future: Navigate to signup screen
    }
}



