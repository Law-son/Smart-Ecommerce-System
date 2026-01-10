package org.example.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.Scene;
import org.example.dto.UserDTO;
import org.example.services.AuthService;
import org.example.services.SignupResult;
import org.example.utils.NavigationHelper;
import org.example.utils.SessionManager;
import org.example.utils.validators.EmailValidator;
import org.example.utils.validators.PasswordValidator;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for the Signup screen.
 * Handles user registration with full validation.
 */
public class SignupController implements Initializable {
    
    @FXML
    private TextField fullNameField;
    
    @FXML
    private TextField emailField;
    
    @FXML
    private PasswordField passwordField;
    
    @FXML
    private PasswordField confirmPasswordField;
    
    @FXML
    private ComboBox<String> roleCombo;
    
    @FXML
    private Button signupButton;
    
    @FXML
    private Button cancelButton;
    
    @FXML
    private Label errorLabel;
    
    private AuthService authService;
    private SessionManager sessionManager;
    private EmailValidator emailValidator;
    private PasswordValidator passwordValidator;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.authService = new AuthService();
        this.sessionManager = SessionManager.getInstance();
        this.emailValidator = new org.example.utils.validators.EmailValidator();
        this.passwordValidator = new org.example.utils.validators.PasswordValidator();
        
        // Clear any existing session
        sessionManager.clearSession();
        
        // Initialize role combo (default: CUSTOMER)
        roleCombo.getItems().addAll("CUSTOMER", "ADMIN");
        roleCombo.setValue("CUSTOMER");
        
        // Set up event handlers
        signupButton.setOnAction(e -> handleSignup());
        cancelButton.setOnAction(e -> handleCancel());
        
        // Clear error label initially
        errorLabel.setText("");
    }
    
    /**
     * Handles signup button click.
     */
    private void handleSignup() {
        try {
            // Clear previous errors
            errorLabel.setText("");
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);
            
            // Get input values
            String fullName = fullNameField.getText().trim();
            String email = emailField.getText().trim();
            String password = passwordField.getText();
            String confirmPassword = confirmPasswordField.getText();
            String role = roleCombo.getValue();
            
            // Validate inputs with specific messages
            if (fullName.isEmpty()) {
                errorLabel.setText("Full name is required");
                errorLabel.setVisible(true);
                errorLabel.setManaged(true);
                return;
            }
            
            if (email.isEmpty()) {
                errorLabel.setText("Email is required");
                errorLabel.setVisible(true);
                errorLabel.setManaged(true);
                return;
            }
            
            // Validate email format
            if (!emailValidator.isValid(email)) {
                errorLabel.setText("Invalid email format. Please enter a valid email address.");
                errorLabel.setVisible(true);
                errorLabel.setManaged(true);
                return;
            }
            
            if (password.isEmpty()) {
                errorLabel.setText("Password is required");
                errorLabel.setVisible(true);
                errorLabel.setManaged(true);
                return;
            }
            
            // Validate password strength before checking match
            if (!passwordValidator.isValid(password)) {
                int minLength = passwordValidator.getMinPasswordLength();
                errorLabel.setText("Password does not meet requirements. Password must be at least " + 
                                 minLength + " characters long and contain both letters and numbers.");
                errorLabel.setVisible(true);
                errorLabel.setManaged(true);
                return;
            }
            
            // Validate passwords match
            if (!password.equals(confirmPassword)) {
                errorLabel.setText("Passwords do not match. Please ensure both password fields are identical.");
                errorLabel.setVisible(true);
                errorLabel.setManaged(true);
                return;
            }
            
            // Create UserDTO
            UserDTO userDTO = new UserDTO();
            userDTO.setFullName(fullName);
            userDTO.setEmail(email);
            userDTO.setPassword(password);
            userDTO.setRole(role != null ? role : "CUSTOMER");
            
            // Attempt signup via AuthService (returns specific error messages)
            SignupResult result = authService.signup(userDTO);
            
            if (result.isSuccess()) {
                // Show success message
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Signup Successful");
                alert.setHeaderText(null);
                alert.setContentText("Account created successfully! Please login with your credentials.");
                alert.showAndWait();
                
                // Navigate back to login screen
                navigateToLogin();
            } else {
                // Display specific error message from AuthService
                errorLabel.setText(result.getErrorMessage());
            }
        } catch (Exception e) {
            System.err.println("Error during signup: " + e.getMessage());
            e.printStackTrace();
            errorLabel.setText("An unexpected error occurred: " + e.getMessage() + ". Please try again.");
        }
    }
    
    /**
     * Handles cancel button click.
     */
    private void handleCancel() {
        navigateToLogin();
    }
    
    /**
     * Navigates to Login screen.
     */
    private void navigateToLogin() {
        try {
            Scene scene = cancelButton.getScene();
            if (scene != null) {
                boolean navigated = NavigationHelper.navigateTo("Login.fxml", NavigationHelper.getStage(scene));
                if (!navigated) {
                    errorLabel.setText("Failed to navigate to login screen.");
                }
            }
        } catch (Exception e) {
            System.err.println("Error navigating to login: " + e.getMessage());
            e.printStackTrace();
            errorLabel.setText("Navigation error. Please try again.");
        }
    }
}

