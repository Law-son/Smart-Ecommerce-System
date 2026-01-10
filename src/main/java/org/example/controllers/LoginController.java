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
            
            // Validate input with specific messages
            if (email.isEmpty()) {
                errorLabel.setText("Email is required");
                return;
            }
            
            if (password.isEmpty()) {
                errorLabel.setText("Password is required");
                return;
            }
            
            // Attempt login via AuthService (returns specific error messages)
            org.example.services.AuthResult result = authService.login(email, password);
            
            if (result.isSuccess()) {
                // Store user in session (session caching)
                User user = result.getUser();
                sessionManager.setCurrentUser(user);
                
                // Log successful login with user details
                System.out.println("User logged in: " + user.getFullName() + " (Role: " + user.getRole() + ")");
                
                // Navigate based on user role - Admin goes directly to Admin panel
                Scene scene = loginButton.getScene();
                if (scene != null) {
                    boolean navigated;
                    String targetScreen;
                    
                    // Admin users go directly to Admin panel, customers go to Catalog
                    if (sessionManager.isAdmin()) {
                        targetScreen = "Admin.fxml";
                    } else {
                        targetScreen = "Catalog.fxml";
                    }
                    
                    navigated = NavigationHelper.navigateTo(targetScreen, NavigationHelper.getStage(scene));
                    if (!navigated) {
                        errorLabel.setText("Failed to navigate to " + targetScreen + ". Please try again.");
                        errorLabel.setVisible(true);
                        errorLabel.setManaged(true);
                    }
                } else {
                    errorLabel.setText("Navigation error. Please try again.");
                    errorLabel.setVisible(true);
                    errorLabel.setManaged(true);
                }
            } else {
                // Display specific error message from AuthService
                errorLabel.setText(result.getErrorMessage());
                errorLabel.setVisible(true);
                errorLabel.setManaged(true);
            }
        } catch (Exception e) {
            System.err.println("Error during login: " + e.getMessage());
            e.printStackTrace();
            errorLabel.setText("An unexpected error occurred: " + e.getMessage() + ". Please try again.");
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
        }
    }
    
    /**
     * Handles signup button click.
     * Navigates to signup screen.
     */
    private void handleSignup() {
        try {
            errorLabel.setText("");
            Scene scene = signupButton.getScene();
            if (scene != null) {
                boolean navigated = NavigationHelper.navigateTo("Signup.fxml", NavigationHelper.getStage(scene));
                if (!navigated) {
                    errorLabel.setText("Failed to navigate to signup screen.");
                }
            } else {
                errorLabel.setText("Navigation error. Please try again.");
            }
        } catch (Exception e) {
            System.err.println("Error navigating to signup: " + e.getMessage());
            e.printStackTrace();
            errorLabel.setText("Navigation error. Please try again.");
        }
    }
}



