package org.example.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.Scene;
import org.example.models.Order;
import org.example.services.OrderService;
import org.example.utils.NavigationHelper;
import org.example.utils.SessionManager;

import java.math.BigDecimal;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller for the Orders screen.
 * Handles order history display for customers and admins.
 */
public class OrdersController implements Initializable {
    
    @FXML
    private TableView<Order> ordersTable;
    
    @FXML
    private TableColumn<Order, Integer> orderIdCol;
    
    @FXML
    private TableColumn<Order, java.time.LocalDateTime> orderDateCol;
    
    @FXML
    private TableColumn<Order, String> orderStatusCol;
    
    @FXML
    private TableColumn<Order, BigDecimal> orderTotalCol;
    
    @FXML
    private Label ordersPerfLabel;
    
    @FXML
    private Button refreshOrdersBtn;
    
    @FXML
    private Button backButton;
    
    @FXML
    private ProgressIndicator loadingIndicator;
    
    @FXML
    private Button markReceivedBtn;
    
    private OrderService orderService;
    private SessionManager sessionManager;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.orderService = new OrderService();
        this.sessionManager = SessionManager.getInstance();
        
        // Check if user is logged in
        if (!sessionManager.isLoggedIn()) {
            navigateToLogin();
            return;
        }
        
        // Initialize table columns
        orderIdCol.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        orderDateCol.setCellValueFactory(new PropertyValueFactory<>("orderDate"));
        orderStatusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        orderTotalCol.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        
        // Set up event handlers
        refreshOrdersBtn.setOnAction(e -> loadOrders());
        backButton.setOnAction(e -> navigateToCatalog());
        markReceivedBtn.setOnAction(e -> handleMarkReceived());
        
        // Load orders
        loadOrders();
    }
    
    private void handleMarkReceived() {
        Order selected = ordersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            ordersPerfLabel.setText("Please select an order to mark as received");
            return;
        }
        
        if ("RECEIVED".equals(selected.getStatus())) {
            ordersPerfLabel.setText("Order is already marked as received");
            return;
        }
        
        try {
            boolean success = orderService.updateOrderStatus(selected.getOrderId(), "RECEIVED");
            if (success) {
                ordersPerfLabel.setText("Order #" + selected.getOrderId() + " marked as received");
                loadOrders();
            } else {
                ordersPerfLabel.setText("Failed to update order status");
            }
        } catch (Exception e) {
            System.err.println("Error marking order as received: " + e.getMessage());
            ordersPerfLabel.setText("An error occurred. Please try again.");
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
     * Loads orders based on user role.
     */
    private void loadOrders() {
        try {
            showLoading(true);
            List<Order> orders;
            
            if (sessionManager.isAdmin()) {
                // Admin sees all orders
                orders = orderService.getAllOrders();
            } else {
                // Customer sees only their orders
                int userId = sessionManager.getCurrentUserId();
                if (userId <= 0) {
                    ordersPerfLabel.setText("Error: Invalid user session");
                    ordersTable.getItems().clear();
                    showLoading(false);
                    return;
                }
                orders = orderService.getOrdersByUser(userId);
            }
            
            if (orders == null) {
                orders = new java.util.ArrayList<>();
                ordersPerfLabel.setText("Error: Failed to load orders");
                ordersTable.getItems().clear();
                showLoading(false);
                return;
            }
            
            ordersTable.getItems().clear();
            ordersTable.getItems().addAll(orders);
            
            // Performance timing is logged by OrderService via PerformanceMonitor
            ordersPerfLabel.setText("Loaded " + orders.size() + " orders");
            showLoading(false);
        } catch (Exception e) {
            System.err.println("Error loading orders: " + e.getMessage());
            e.printStackTrace();
            ordersPerfLabel.setText("Error loading orders. Please try again.");
            ordersTable.getItems().clear();
            showLoading(false);
        }
    }
    
    /**
     * Navigates to Login screen.
     */
    private void navigateToLogin() {
        Scene scene = refreshOrdersBtn.getScene();
        if (scene != null) {
            NavigationHelper.navigateTo("Login.fxml", NavigationHelper.getStage(scene));
        }
    }
}



