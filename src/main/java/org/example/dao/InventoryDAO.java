package org.example.dao;

import org.example.configs.DatabaseConfig;
import org.example.dto.InventoryDTO;
import org.example.models.Inventory;

import java.sql.*;
import java.time.LocalDateTime;

/**
 * Data Access Object for Inventory operations.
 * Handles all database interactions for Inventory entities.
 */
public class InventoryDAO {

    /**
     * Retrieves inventory information for a specific product.
     *
     * @param productId The product ID
     * @return Inventory object if found, null otherwise
     */
    public Inventory getInventoryByProduct(int productId) {
        String sql = "SELECT inventory_id, product_id, quantity, last_updated FROM inventory WHERE product_id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, productId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Inventory inventory = new Inventory();
                    inventory.setInventoryId(rs.getInt("inventory_id"));
                    inventory.setProductId(rs.getInt("product_id"));
                    inventory.setQuantity(rs.getInt("quantity"));
                    Timestamp lastUpdated = rs.getTimestamp("last_updated");
                    if (lastUpdated != null) {
                        inventory.setLastUpdated(lastUpdated.toLocalDateTime());
                    }
                    return inventory;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving inventory by product: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }

    /**
     * Updates stock quantity for a product.
     *
     * @param productId The product ID
     * @param dto The inventory data transfer object
     * @return true if stock was updated successfully, false otherwise
     */
    public boolean updateStock(int productId, InventoryDTO dto) {
        String sql = "UPDATE inventory SET quantity = ?, last_updated = CURRENT_TIMESTAMP WHERE product_id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, dto.getQuantity());
            pstmt.setInt(2, productId);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating stock: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Checks if sufficient stock is available for a product.
     *
     * @param productId The product ID
     * @param quantity The required quantity
     * @return true if sufficient stock is available, false otherwise
     */
    public boolean checkStockAvailable(int productId, int quantity) {
        String sql = "SELECT quantity FROM inventory WHERE product_id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, productId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int availableQuantity = rs.getInt("quantity");
                    return availableQuantity >= quantity;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking stock availability: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
}

