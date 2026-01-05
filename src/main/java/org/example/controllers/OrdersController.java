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
        
        // Load orders
        loadOrders();
    }
    
    /**
     * Loads orders based on user role.
     */
    private void loadOrders() {
        long startTime = System.currentTimeMillis();
        
        List<Order> orders;
        
        if (sessionManager.isAdmin()) {
            // Admin sees all orders
            orders = orderService.getAllOrders();
        } else {
            // Customer sees only their orders
            int userId = sessionManager.getCurrentUserId();
            orders = orderService.getOrdersByUser(userId);
        }
        
        ordersTable.getItems().clear();
        ordersTable.getItems().addAll(orders);
        
        long endTime = System.currentTimeMillis();
        ordersPerfLabel.setText("Loaded " + orders.size() + " orders in " + (endTime - startTime) + " ms");
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

