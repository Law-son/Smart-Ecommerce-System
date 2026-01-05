package org.example.services;

import org.example.dao.InventoryDAO;
import org.example.dto.InventoryDTO;
import org.example.models.Inventory;
import org.example.utils.PerformanceMonitor;

/**
 * Inventory service handling stock validation and updates.
 * Follows Single Responsibility Principle by delegating performance monitoring to PerformanceMonitor.
 */
public class InventoryService {
    private final InventoryDAO inventoryDAO;
    private final PerformanceMonitor performanceMonitor;
    
    public InventoryService() {
        this.inventoryDAO = new InventoryDAO();
        this.performanceMonitor = new PerformanceMonitor();
    }
    
    /**
     * Checks if sufficient stock is available for a product.
     * Times the operation for performance monitoring.
     *
     * @param productId Product ID
     * @param quantity Required quantity
     * @return true if sufficient stock is available, false otherwise
     */
    public boolean checkStock(int productId, int quantity) {
        return performanceMonitor.monitor(
            "Stock check for product " + productId,
            () -> {
                boolean available = inventoryDAO.checkStockAvailable(productId, quantity);
                if (!available) {
                    int availableStock = getAvailableStock(productId);
                    System.err.println("Insufficient stock for product " + productId + 
                                     ". Requested " + quantity + ", Available " + availableStock);
                }
                return available;
            }
        );
    }
    
    /**
     * Gets the available stock quantity for a product.
     *
     * @param productId Product ID
     * @return Available quantity, or 0 if product not found
     */
    public int getAvailableStock(int productId) {
        Inventory inventory = inventoryDAO.getInventoryByProduct(productId);
        if (inventory != null) {
            return inventory.getQuantity();
        }
        return 0;
    }
    
    /**
     * Updates stock quantity for a product.
     * Logs cache invalidation message.
     *
     * @param productId Product ID
     * @param quantity New quantity
     * @return true if update successful, false otherwise
     */
    public boolean updateStock(int productId, int quantity) {
        if (quantity < 0) {
            System.err.println("Stock quantity cannot be negative");
            return false;
        }
        
        InventoryDTO dto = new InventoryDTO(productId, quantity);
        boolean success = inventoryDAO.updateStock(productId, dto);
        
        if (success) {
            System.out.println("[CACHE] Stock updated for product " + productId + 
                             " - cache should be invalidated if needed");
        }
        
        return success;
    }
    
    /**
     * Reduces stock quantity for a product (used after order placement).
     *
     * @param productId Product ID
     * @param quantity Quantity to reduce
     * @return true if reduction successful, false otherwise
     */
    public boolean reduceStock(int productId, int quantity) {
        Inventory currentInventory = inventoryDAO.getInventoryByProduct(productId);
        if (currentInventory == null) {
            System.err.println("Product inventory not found: " + productId);
            return false;
        }
        
        int newQuantity = currentInventory.getQuantity() - quantity;
        if (newQuantity < 0) {
            System.err.println("Cannot reduce stock below zero for product: " + productId);
            return false;
        }
        
        return updateStock(productId, newQuantity);
    }
}
