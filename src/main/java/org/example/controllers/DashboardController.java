package org.example.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.Scene;
import org.example.utils.NavigationHelper;
import org.example.utils.SessionManager;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for the Dashboard screen.
 * Handles role-based navigation and user welcome message.
 */
public class DashboardController implements Initializable {
    
    @FXML
    private Label welcomeLabel;
    
    @FXML
    private Button catalogButton;
    
    @FXML
    private Button cartButton;
    
    @FXML
    private Button ordersButton;
    
    @FXML
    private Button adminButton;
    
    @FXML
    private Button logoutButton;
    
    private SessionManager sessionManager;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.sessionManager = SessionManager.getInstance();
        
        // Check if user is logged in
        if (!sessionManager.isLoggedIn()) {
            // Redirect to login if not logged in
            navigateToLogin();
            return;
        }
        
        // Set welcome message
        String userName = sessionManager.getCurrentUser().getFullName();
        welcomeLabel.setText("Welcome, " + userName + "!");
        
        // Show/hide admin button based on role
        boolean isAdmin = sessionManager.isAdmin();
        adminButton.setVisible(isAdmin);
        adminButton.setManaged(isAdmin);
        
        // Set up event handlers
        catalogButton.setOnAction(e -> navigateToCatalog());
        cartButton.setOnAction(e -> navigateToCart());
        ordersButton.setOnAction(e -> navigateToOrders());
        adminButton.setOnAction(e -> navigateToAdmin());
        logoutButton.setOnAction(e -> handleLogout());
    }
    
    /**
     * Navigates to Catalog screen.
     */
    private void navigateToCatalog() {
        Scene scene = catalogButton.getScene();
        if (scene != null) {
            NavigationHelper.navigateTo("Catalog.fxml", NavigationHelper.getStage(scene));
        }
    }
    
    /**
     * Navigates to Cart screen.
     */
    private void navigateToCart() {
        Scene scene = cartButton.getScene();
        if (scene != null) {
            NavigationHelper.navigateTo("Cart.fxml", NavigationHelper.getStage(scene));
        }
    }
    
    /**
     * Navigates to Orders screen.
     */
    private void navigateToOrders() {
        Scene scene = ordersButton.getScene();
        if (scene != null) {
            NavigationHelper.navigateTo("Orders.fxml", NavigationHelper.getStage(scene));
        }
    }
    
    /**
     * Navigates to Admin screen (only for ADMIN role).
     */
    private void navigateToAdmin() {
        if (!sessionManager.isAdmin()) {
            System.err.println("Unauthorized access attempt to Admin panel");
            return;
        }
        
        Scene scene = adminButton.getScene();
        if (scene != null) {
            NavigationHelper.navigateTo("Admin.fxml", NavigationHelper.getStage(scene));
        }
    }
    
    /**
     * Handles logout action.
     */
    private void handleLogout() {
        // Clear session via AuthService
        org.example.services.AuthService authService = new org.example.services.AuthService();
        authService.logout();
        
        // Clear session manager
        sessionManager.clearSession();
        
        // Navigate to login
        navigateToLogin();
    }
    
    /**
     * Navigates to Login screen.
     */
    private void navigateToLogin() {
        Scene scene = logoutButton.getScene();
        if (scene != null) {
            NavigationHelper.navigateTo("Login.fxml", NavigationHelper.getStage(scene));
        }
    }
}



