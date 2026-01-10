package org.example.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.Scene;
import org.example.dao.ProductDAO;
import org.example.dto.OrderItemDTO;
import org.example.models.Product;
import org.example.services.CartService;
import org.example.services.OrderService;
import org.example.utils.NavigationHelper;
import org.example.utils.SessionManager;

import java.math.BigDecimal;
import java.util.List;
import java.util.ResourceBundle;
import java.net.URL;

/**
 * Controller for the Cart screen.
 * Handles cart item management and checkout.
 */
public class CartController implements Initializable {
    
    @FXML
    private ListView<String> cartItemsList;
    
    @FXML
    private Label cartTotalLabel;
    
    @FXML
    private Button updateItemButton;
    
    @FXML
    private Button removeItemButton;
    
    @FXML
    private Button checkoutButton;
    
    @FXML
    private Label cartErrorLabel;
    
    @FXML
    private Button backButton;

    @FXML
    private Button refreshButton;

    @FXML
    private ProgressIndicator loadingIndicator;
    
    private CartService cartService;
    private OrderService orderService;
    private ProductDAO productDAO;
    private SessionManager sessionManager;
    private List<OrderItemDTO> currentCartItems = new java.util.ArrayList<>();
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            this.cartService = CartService.getInstance();
            this.orderService = new OrderService();
            this.productDAO = new ProductDAO();
            this.sessionManager = SessionManager.getInstance();
            
            // Check if user is logged in
            if (!sessionManager.isLoggedIn()) {
                navigateToLogin();
                return;
            }
            
            // Set up event handlers
            updateItemButton.setOnAction(e -> handleUpdateItem());
            removeItemButton.setOnAction(e -> handleRemoveItem());
            checkoutButton.setOnAction(e -> handleCheckout());
            backButton.setOnAction(e -> navigateToCatalog());
            refreshButton.setOnAction(e -> loadCartItems());
            
            // Clear error label initially
            hideError();
            
            // Load cart items
            loadCartItems();
        } catch (Exception e) {
            System.err.println("Error initializing CartController: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void showError(String message) {
        if (cartErrorLabel != null) {
            cartErrorLabel.setText(message);
            cartErrorLabel.setVisible(true);
            cartErrorLabel.setManaged(true);
        }
    }

    private void hideError() {
        if (cartErrorLabel != null) {
            cartErrorLabel.setText("");
            cartErrorLabel.setVisible(false);
            cartErrorLabel.setManaged(false);
        }
    }

    /**
     * Shows or hides the loading indicator.
     */
    private void showLoading(boolean show) {
        if (loadingIndicator != null) {
            loadingIndicator.setVisible(show);
            loadingIndicator.setManaged(show);
        }
    }
    
    /**
     * Navigates back to catalog.
     */
    private void navigateToCatalog() {
        Scene scene = backButton.getScene();
        if (scene != null) {
            NavigationHelper.navigateTo("Catalog.fxml", NavigationHelper.getStage(scene));
        }
    }
    
    /**
     * Loads and displays cart items.
     */
    private void loadCartItems() {
        showLoading(true);
        javafx.application.Platform.runLater(() -> {
            try {
                hideError();
                currentCartItems = cartService.getCartItems();
                cartItemsList.getItems().clear();
                
                if (currentCartItems.isEmpty()) {
                    cartItemsList.getItems().add("Your cart is empty");
                    cartTotalLabel.setText("$0.00");
                    return;
                }
                
                // Display cart items
                for (OrderItemDTO item : currentCartItems) {
                    Product product = productDAO.getProductById(item.getProductId());
                    String productName = product != null ? product.getName() : "Product #" + item.getProductId();
                    String itemText = String.format("%s - Qty: %d - $%.2f", 
                        productName, 
                        item.getQuantity(), 
                        item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
                    cartItemsList.getItems().add(itemText);
                }
                
                // Update total
                BigDecimal total = cartService.calculateCartTotal();
                cartTotalLabel.setText("$" + String.format("%.2f", total));
            } catch (Exception e) {
                System.err.println("Error loading cart items: " + e.getMessage());
                showError("Error loading cart. Please refresh.");
            } finally {
                showLoading(false);
            }
        });
    }
    
    /**
     * Handles update item button click.
     */
    private void handleUpdateItem() {
        try {
            hideError();
            int selectedIndex = cartItemsList.getSelectionModel().getSelectedIndex();
            
            if (currentCartItems.isEmpty()) {
                showError("Your cart is empty.");
                return;
            }
            
            if (selectedIndex < 0 || selectedIndex >= currentCartItems.size()) {
                showError("Please select an item from the list to update.");
                return;
            }
            
            OrderItemDTO item = currentCartItems.get(selectedIndex);
            
            // Show input dialog for new quantity
            TextInputDialog dialog = new TextInputDialog(String.valueOf(item.getQuantity()));
            dialog.setTitle("Update Quantity");
            dialog.setHeaderText("Update quantity for " + getProductName(item.getProductId()));
            dialog.setContentText("Enter new quantity:");
            
            dialog.showAndWait().ifPresent(quantityStr -> {
                try {
                    int newQuantity = Integer.parseInt(quantityStr.trim());
                    if (newQuantity <= 0) {
                        showError("Quantity must be greater than zero.");
                        return;
                    }
                    
                    boolean success = cartService.updateCartItem(item.getProductId(), newQuantity);
                    if (success) {
                        loadCartItems();
                    } else {
                        showError("Failed to update item. Check stock availability.");
                    }
                } catch (NumberFormatException e) {
                    showError("Invalid quantity. Please enter a valid number.");
                }
            });
        } catch (Exception e) {
            System.err.println("Error in handleUpdateItem: " + e.getMessage());
            showError("An error occurred. Please try again.");
        }
    }
    
    /**
     * Handles remove item button click.
     */
    private void handleRemoveItem() {
        try {
            hideError();
            int selectedIndex = cartItemsList.getSelectionModel().getSelectedIndex();
            
            if (currentCartItems.isEmpty()) {
                showError("Your cart is empty.");
                return;
            }
            
            if (selectedIndex < 0 || selectedIndex >= currentCartItems.size()) {
                showError("Please select an item from the list to remove.");
                return;
            }
            
            OrderItemDTO item = currentCartItems.get(selectedIndex);
            
            boolean success = cartService.removeFromCart(item.getProductId());
            if (success) {
                loadCartItems();
            } else {
                showError("Failed to remove item.");
            }
        } catch (Exception e) {
            System.err.println("Error in handleRemoveItem: " + e.getMessage());
            showError("An error occurred. Please try again.");
        }
    }
    
    /**
     * Handles checkout button click.
     */
    private void handleCheckout() {
        try {
            hideError();
            
            if (currentCartItems == null || currentCartItems.isEmpty()) {
                showError("Cart is empty. Cannot checkout.");
                return;
            }
            
            // Validate user is logged in
            if (!sessionManager.isLoggedIn()) {
                showError("You must be logged in to checkout.");
                navigateToLogin();
                return;
            }
            
            // Calculate cart total
            BigDecimal cartTotal = cartService.calculateCartTotal();
            if (cartTotal == null || cartTotal.compareTo(BigDecimal.ZERO) <= 0) {
                showError("Invalid cart total. Please check your cart items.");
                return;
            }
            
            // Create order via OrderService (this validates stock internally)
            int userId = sessionManager.getCurrentUserId();
            if (userId <= 0) {
                showError("Invalid user session. Please login again.");
                navigateToLogin();
                return;
            }
            
            int orderId = orderService.createOrder(userId, currentCartItems);
            
            if (orderId > 0) {
                // Clear cart after successful order creation
                cartService.clearCart();
                
                // Show success message
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Order Placed");
                alert.setHeaderText(null);
                alert.setContentText("Order #" + orderId + " has been placed successfully!\nTotal: $" + String.format("%.2f", cartTotal));
                alert.showAndWait();
                
                // Navigate to orders screen
                navigateToOrders();
            } else {
                showError("Checkout failed. One or more items might be out of stock.");
            }
        } catch (Exception e) {
            System.err.println("Error during checkout: " + e.getMessage());
            e.printStackTrace();
            showError("An error occurred during checkout. Please try again.");
        }
    }
    
    /**
     * Gets product name by ID.
     */
    private String getProductName(int productId) {
        Product product = productDAO.getProductById(productId);
        return product != null ? product.getName() : "Product #" + productId;
    }
    
    /**
     * Navigates to Orders screen.
     */
    private void navigateToOrders() {
        Scene scene = checkoutButton.getScene();
        if (scene != null) {
            NavigationHelper.navigateTo("Orders.fxml", NavigationHelper.getStage(scene));
        }
    }
    
    /**
     * Navigates to Login screen.
     */
    private void navigateToLogin() {
        Scene scene = checkoutButton.getScene();
        if (scene != null) {
            NavigationHelper.navigateTo("Login.fxml", NavigationHelper.getStage(scene));
        }
    }
}



