package org.example.dao;

import org.example.configs.DatabaseConfig;
import org.example.dto.OrderItemDTO;
import org.example.models.OrderItem;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for OrderItem operations.
 * Handles all database interactions for OrderItem entities.
 */
public class OrderItemDAO {

    /**
     * Retrieves all order items for a specific order.
     *
     * @param orderId The order ID
     * @return List of OrderItem objects for the order
     */
    public List<OrderItem> getItemsByOrder(int orderId) {
        String sql = "SELECT order_item_id, order_id, product_id, quantity, unit_price FROM order_items WHERE order_id = ?";
        List<OrderItem> items = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, orderId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    OrderItem item = new OrderItem();
                    item.setOrderItemId(rs.getInt("order_item_id"));
                    item.setOrderId(rs.getInt("order_id"));
                    item.setProductId(rs.getInt("product_id"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setUnitPrice(rs.getBigDecimal("unit_price"));
                    items.add(item);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving order items: " + e.getMessage());
            e.printStackTrace();
        }
        
        return items;
    }

    /**
     * Adds order items to an order.
     * This method can be called with an existing connection for transaction support.
     *
     * @param orderId The order ID
     * @param items List of OrderItemDTO objects
     * @return true if items were added successfully, false otherwise
     */
    public boolean addOrderItems(int orderId, List<OrderItemDTO> items) {
        return addOrderItems(orderId, items, null);
    }

    /**
     * Adds order items to an order with transaction support.
     *
     * @param orderId The order ID
     * @param items List of OrderItemDTO objects
     * @param conn Existing database connection (for transaction support), or null to create new connection
     * @return true if items were added successfully, false otherwise
     */
    public boolean addOrderItems(int orderId, List<OrderItemDTO> items, Connection conn) {
        String sql = "INSERT INTO order_items (order_id, product_id, quantity, unit_price) VALUES (?, ?, ?, ?)";
        
        boolean useExternalConnection = conn != null;
        
        try {
            if (!useExternalConnection) {
                conn = DatabaseConfig.getConnection();
            }
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                for (OrderItemDTO item : items) {
                    pstmt.setInt(1, orderId);
                    pstmt.setInt(2, item.getProductId());
                    pstmt.setInt(3, item.getQuantity());
                    pstmt.setBigDecimal(4, item.getUnitPrice());
                    pstmt.addBatch();
                }
                
                int[] results = pstmt.executeBatch();
                for (int result : results) {
                    if (result == 0) {
                        return false;
                    }
                }
                
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("Error adding order items: " + e.getMessage());
            if (e.getSQLState().equals("23503")) { // Foreign key constraint violation
                System.err.println("Order ID or Product ID does not exist");
            }
            e.printStackTrace();
            return false;
        } finally {
            if (!useExternalConnection && conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("Error closing connection: " + e.getMessage());
                }
            }
        }
    }
}

