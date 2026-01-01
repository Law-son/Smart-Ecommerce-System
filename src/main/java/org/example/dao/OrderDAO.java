package org.example.dao;

import org.example.configs.DatabaseConfig;
import org.example.dto.OrderDTO;
import org.example.dto.OrderItemDTO;
import org.example.models.Order;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Order operations.
 * Handles all database interactions for Order entities.
 */
public class OrderDAO {

    /**
     * Retrieves an order by ID.
     *
     * @param orderId The order ID
     * @return Order object if found, null otherwise
     */
    public Order getOrderById(int orderId) {
        String sql = "SELECT order_id, user_id, order_date, status, total_amount FROM orders WHERE order_id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, orderId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Order order = new Order();
                    order.setOrderId(rs.getInt("order_id"));
                    order.setUserId(rs.getInt("user_id"));
                    Timestamp orderDate = rs.getTimestamp("order_date");
                    if (orderDate != null) {
                        order.setOrderDate(orderDate.toLocalDateTime());
                    }
                    order.setStatus(rs.getString("status"));
                    order.setTotalAmount(rs.getBigDecimal("total_amount"));
                    return order;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving order by ID: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }

    /**
     * Retrieves all orders for a specific user.
     *
     * @param userId The user ID
     * @return List of Order objects for the user
     */
    public List<Order> getOrdersByUser(int userId) {
        String sql = "SELECT order_id, user_id, order_date, status, total_amount FROM orders WHERE user_id = ? ORDER BY order_date DESC";
        List<Order> orders = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Order order = new Order();
                    order.setOrderId(rs.getInt("order_id"));
                    order.setUserId(rs.getInt("user_id"));
                    Timestamp orderDate = rs.getTimestamp("order_date");
                    if (orderDate != null) {
                        order.setOrderDate(orderDate.toLocalDateTime());
                    }
                    order.setStatus(rs.getString("status"));
                    order.setTotalAmount(rs.getBigDecimal("total_amount"));
                    orders.add(order);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving orders by user: " + e.getMessage());
            e.printStackTrace();
        }
        
        return orders;
    }

    /**
     * Retrieves all orders (Admin only).
     *
     * @return List of all Order objects
     */
    public List<Order> getAllOrders() {
        String sql = "SELECT order_id, user_id, order_date, status, total_amount FROM orders ORDER BY order_date DESC";
        List<Order> orders = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                Order order = new Order();
                order.setOrderId(rs.getInt("order_id"));
                order.setUserId(rs.getInt("user_id"));
                Timestamp orderDate = rs.getTimestamp("order_date");
                if (orderDate != null) {
                    order.setOrderDate(orderDate.toLocalDateTime());
                }
                order.setStatus(rs.getString("status"));
                order.setTotalAmount(rs.getBigDecimal("total_amount"));
                orders.add(order);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving all orders: " + e.getMessage());
            e.printStackTrace();
        }
        
        return orders;
    }

    /**
     * Creates a new order with order items.
     * This method creates the order and its items in a transaction.
     *
     * @param orderDTO The order data transfer object containing order and items
     * @return true if order was created successfully, false otherwise
     */
    public boolean createOrder(OrderDTO orderDTO) {
        String orderSql = "INSERT INTO orders (user_id, status, total_amount) VALUES (?, ?, ?)";
        
        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();
            conn.setAutoCommit(false); // Start transaction
            
            // Create the order
            try (PreparedStatement orderStmt = conn.prepareStatement(orderSql, Statement.RETURN_GENERATED_KEYS)) {
                orderStmt.setInt(1, orderDTO.getUserId());
                orderStmt.setString(2, orderDTO.getStatus());
                orderStmt.setBigDecimal(3, orderDTO.getTotalAmount());
                
                int rowsAffected = orderStmt.executeUpdate();
                if (rowsAffected == 0) {
                    conn.rollback();
                    return false;
                }
                
                // Get the generated order ID
                int orderId;
                try (ResultSet generatedKeys = orderStmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        orderId = generatedKeys.getInt(1);
                    } else {
                        conn.rollback();
                        return false;
                    }
                }
                
                // Create order items
                OrderItemDAO orderItemDAO = new OrderItemDAO();
                if (!orderItemDAO.addOrderItems(orderId, orderDTO.getItems(), conn)) {
                    conn.rollback();
                    return false;
                }
                
                conn.commit();
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("Error creating order: " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    System.err.println("Error rolling back transaction: " + rollbackEx.getMessage());
                }
            }
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("Error closing connection: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Updates the status of an order.
     *
     * @param orderId The order ID
     * @param status The new status
     * @return true if order status was updated successfully, false otherwise
     */
    public boolean updateOrderStatus(int orderId, String status) {
        String sql = "UPDATE orders SET status = ? WHERE order_id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, status);
            pstmt.setInt(2, orderId);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating order status: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}

