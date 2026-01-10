package org.example.utils.cache;

import java.util.HashMap;
import java.util.Map;

/**
 * Cache manager for product ratings.
 * Follows Single Responsibility Principle - only handles rating caching.
 */
public class RatingCacheManager {
    
    private static RatingCacheManager instance;
    // Cache for average ratings: productId -> averageRating
    private final Map<Integer, Double> ratingCache;
    
    private RatingCacheManager() {
        this.ratingCache = new HashMap<>();
    }

    public static synchronized RatingCacheManager getInstance() {
        if (instance == null) {
            instance = new RatingCacheManager();
        }
        return instance;
    }
    
    /**
     * Gets average rating from cache.
     *
     * @param productId Product ID
     * @return Average rating if found in cache, null otherwise
     */
    public Double get(int productId) {
        Double rating = ratingCache.get(productId);
        if (rating != null) {
            System.out.println("[CACHE] Rating cache HIT for product: " + productId + " (rating: " + rating + ")");
        } else {
            System.out.println("[CACHE] Rating cache MISS for product: " + productId);
        }
        return rating;
    }
    
    /**
     * Puts average rating into cache.
     * Only caches after successful computation.
     *
     * @param productId Product ID
     * @param rating Average rating
     */
    public void put(int productId, double rating) {
        ratingCache.put(productId, rating);
        System.out.println("[CACHE] Rating cache LOADED for product: " + productId + " (rating: " + rating + ")");
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



