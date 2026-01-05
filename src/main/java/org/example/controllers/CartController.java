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
    
    private CartService cartService;
    private OrderService orderService;
    private ProductDAO productDAO;
    private SessionManager sessionManager;
    private List<OrderItemDTO> currentCartItems;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.cartService = new CartService();
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
        
        // Load cart items
        loadCartItems();
    }
    
    /**
     * Loads and displays cart items.
     */
    private void loadCartItems() {
        currentCartItems = cartService.getCartItems();
        cartItemsList.getItems().clear();
        cartErrorLabel.setText("");
        
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
        cartTotalLabel.setText("$" + total);
    }
    
    /**
     * Handles update item button click.
     */
    private void handleUpdateItem() {
        int selectedIndex = cartItemsList.getSelectionModel().getSelectedIndex();
        if (selectedIndex < 0 || selectedIndex >= currentCartItems.size()) {
            cartErrorLabel.setText("Please select an item to update");
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
                int newQuantity = Integer.parseInt(quantityStr);
                if (newQuantity <= 0) {
                    cartErrorLabel.setText("Quantity must be greater than zero");
                    return;
                }
                
                boolean success = cartService.updateCartItem(item.getProductId(), newQuantity);
                if (success) {
                    loadCartItems();
                    cartErrorLabel.setText("");
                } else {
                    cartErrorLabel.setText("Failed to update item. Check stock availability.");
                }
            } catch (NumberFormatException e) {
                cartErrorLabel.setText("Invalid quantity. Please enter a number.");
            }
        });
    }
    
    /**
     * Handles remove item button click.
     */
    private void handleRemoveItem() {
        int selectedIndex = cartItemsList.getSelectionModel().getSelectedIndex();
        if (selectedIndex < 0 || selectedIndex >= currentCartItems.size()) {
            cartErrorLabel.setText("Please select an item to remove");
            return;
        }
        
        OrderItemDTO item = currentCartItems.get(selectedIndex);
        
        boolean success = cartService.removeFromCart(item.getProductId());
        if (success) {
            loadCartItems();
            cartErrorLabel.setText("");
        } else {
            cartErrorLabel.setText("Failed to remove item");
        }
    }
    
    /**
     * Handles checkout button click.
     */
    private void handleCheckout() {
        if (currentCartItems.isEmpty()) {
            cartErrorLabel.setText("Cart is empty. Cannot checkout.");
            return;
        }
        
        // Validate stock for all items
        for (OrderItemDTO item : currentCartItems) {
            // Stock validation is done in CartService, but we can double-check here
            // The OrderService will also validate before creating order
        }
        
        // Create order via OrderService
        int userId = sessionManager.getCurrentUserId();
        int orderId = orderService.createOrder(userId, currentCartItems);
        
        if (orderId > 0) {
            // Clear cart
            cartService.clearCart();
            
            // Show success message
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Order Placed");
            alert.setHeaderText(null);
            alert.setContentText("Order #" + orderId + " has been placed successfully!");
            alert.showAndWait();
            
            // Navigate to orders screen
            navigateToOrders();
        } else {
            cartErrorLabel.setText("Failed to create order. Please check stock availability and try again.");
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

