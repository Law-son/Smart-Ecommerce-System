package org.example.services;

import org.example.dao.InventoryDAO;
import org.example.dto.InventoryDTO;
import org.example.models.Inventory;
import org.example.utils.PerformanceMonitor;
import org.example.utils.cache.StockCacheManager;

/**
 * Inventory service handling stock validation and updates.
 * Follows Single Responsibility Principle by delegating performance monitoring and caching to dedicated classes.
 */
public class InventoryService {
    private final InventoryDAO inventoryDAO;
    private final PerformanceMonitor performanceMonitor;
    private final StockCacheManager stockCache;
    
    public InventoryService() {
        this.inventoryDAO = new InventoryDAO();
        this.performanceMonitor = new PerformanceMonitor();
        this.stockCache = new StockCacheManager();
    }
    
    /**
     * Checks if sufficient stock is available for a product.
     * Uses cache-first strategy with performance monitoring.
     *
     * @param productId Product ID
     * @param quantity Required quantity
     * @return true if sufficient stock is available, false otherwise
     */
    public boolean checkStock(int productId, int quantity) {
        return performanceMonitor.monitor(
            "Stock validation",
            () -> {
                // Check cache first
                Integer cachedStock = stockCache.get(productId);
                if (cachedStock != null) {
                    boolean available = cachedStock >= quantity;
                    if (!available) {
                        System.err.println("Insufficient stock for product " + productId + 
                                         ". Requested " + quantity + ", Available " + cachedStock);
                    }
                    return available;
                }
                
                // Cache miss - fetch from database
                boolean available = inventoryDAO.checkStockAvailable(productId, quantity);
                if (!available) {
                    int availableStock = getAvailableStock(productId);
                    System.err.println("Insufficient stock for product " + productId + 
                                     ". Requested " + quantity + ", Available " + availableStock);
                } else {
                    // Cache the stock quantity after successful DB fetch
                    int currentStock = getAvailableStock(productId);
                    if (currentStock >= 0) {
                        stockCache.put(productId, currentStock);
                    }
                }
                return available;
            }
        );
    }
    
    /**
     * Gets the available stock quantity for a product.
     * Uses cache-first strategy.
     *
     * @param productId Product ID
     * @return Available quantity, or 0 if product not found
     */
    public int getAvailableStock(int productId) {
        // Check cache first
        Integer cachedStock = stockCache.get(productId);
        if (cachedStock != null) {
            return cachedStock;
        }
        
        // Cache miss - fetch from database
        Inventory inventory = inventoryDAO.getInventoryByProduct(productId);
        if (inventory != null) {
            int quantity = inventory.getQuantity();
            // Cache after successful DB fetch
            stockCache.put(productId, quantity);
            return quantity;
        }
        
        // Product not found - cache as 0 to avoid repeated DB calls
        stockCache.put(productId, 0);
        return 0;
    }
    
    /**
     * Updates stock quantity for a product.
     * Invalidates and updates cache after successful DB update.
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
            // Invalidate old cache and update with new value
            stockCache.invalidate(productId);
            stockCache.put(productId, quantity);
            System.out.println("[CACHE] Stock cache updated for product: " + productId);
        }
        
        return success;
    }
    
    /**
     * Reduces stock quantity for a product (used after order placement).
     * Updates cache after successful reduction.
     *
     * @param productId Product ID
     * @param quantity Quantity to reduce
     * @return true if reduction successful, false otherwise
     */
    public boolean reduceStock(int productId, int quantity) {
        // Get current stock (uses cache if available)
        int currentStock = getAvailableStock(productId);
        
        if (currentStock < quantity) {
            System.err.println("Cannot reduce stock below zero for product: " + productId);
            return false;
        }
        
        int newQuantity = currentStock - quantity;
        return updateStock(productId, newQuantity);
    }
}
