package org.example.utils.cache;

import java.util.HashMap;
import java.util.Map;

/**
 * Cache manager for stock/inventory data.
 * Follows Single Responsibility Principle - only handles stock caching.
 */
public class StockCacheManager {
    
    // Cache for stock quantities: productId -> quantity
    private final Map<Integer, Integer> stockCache;
    
    public StockCacheManager() {
        this.stockCache = new HashMap<>();
    }
    
    /**
     * Gets stock quantity from cache.
     *
     * @param productId Product ID
     * @return Stock quantity if found in cache, null otherwise
     */
    public Integer get(int productId) {
        Integer quantity = stockCache.get(productId);
        if (quantity != null) {
            System.out.println("[CACHE] Stock cache HIT for product: " + productId + " (quantity: " + quantity + ")");
        } else {
            System.out.println("[CACHE] Stock cache MISS for product: " + productId);
        }
        return quantity;
    }
    
    /**
     * Puts stock quantity into cache.
     * Only caches after successful DB fetch.
     *
     * @param productId Product ID
     * @param quantity Stock quantity
     */
    public void put(int productId, int quantity) {
        stockCache.put(productId, quantity);
        System.out.println("[CACHE] Stock cache LOADED for product: " + productId + " (quantity: " + quantity + ")");
    }
    
    /**
     * Invalidates stock cache for a specific product.
     * Called when stock is updated.
     *
     * @param productId Product ID
     */
    public void invalidate(int productId) {
        if (stockCache.remove(productId) != null) {
            System.out.println("[CACHE] Stock cache INVALIDATED for product: " + productId);
        }
    }
    
    /**
     * Clears the entire stock cache.
     */
    public void clear() {
        int size = stockCache.size();
        stockCache.clear();
        System.out.println("[CACHE] Stock cache cleared (" + size + " entries)");
    }
}

