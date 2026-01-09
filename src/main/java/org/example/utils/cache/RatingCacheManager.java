package org.example.utils.cache;

import java.util.HashMap;
import java.util.Map;

/**
 * Cache manager for product ratings.
 * Follows Single Responsibility Principle - only handles rating caching.
 */
public class RatingCacheManager {
    
    // Cache for average ratings: productId -> averageRating
    private final Map<Integer, Double> ratingCache;
    
    public RatingCacheManager() {
        this.ratingCache = new HashMap<>();
    }
    
    /**
     * Gets average rating from cache.
     *
     * @param productId Product ID
     * @return Average rating if found in cache, null otherwise
     */
    public Double get(int productId) {
        return ratingCache.get(productId);
    }
    
    /**
     * Puts average rating into cache.
     *
     * @param productId Product ID
     * @param rating Average rating
     */
    public void put(int productId, double rating) {
        ratingCache.put(productId, rating);
    }
    
    /**
     * Invalidates rating cache for a specific product.
     *
     * @param productId Product ID
     */
    public void invalidate(int productId) {
        if (ratingCache.remove(productId) != null) {
            System.out.println("[CACHE] Rating cache invalidated for product: " + productId);
        }
    }
    
    /**
     * Clears the entire rating cache.
     */
    public void clear() {
        ratingCache.clear();
        System.out.println("[CACHE] Rating cache cleared");
    }
}



