package org.example.utils.cache;

import org.example.dto.ProductDTO;
import org.example.models.Product;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Cache manager for product data.
 * Follows Single Responsibility Principle - only handles product caching.
 */
public class ProductCacheManager {
    
    private final Map<Integer, ProductDTO> productCache;
    private final Map<Integer, Product> productModelCache;
    private List<ProductDTO> productListCache;
    private long cacheTimestamp;
    private static final long CACHE_TTL = 300000; // 5 minutes in milliseconds
    
    public ProductCacheManager() {
        this.productCache = new HashMap<>();
        this.productModelCache = new HashMap<>();
        this.productListCache = new ArrayList<>();
        this.cacheTimestamp = 0;
    }
    
    /**
     * Gets a product from cache by ID.
     *
     * @param productId Product ID
     * @return ProductDTO if found in cache, null otherwise
     */
    public ProductDTO getById(int productId) {
        ProductDTO cached = productCache.get(productId);
        if (cached != null) {
            System.out.println("[CACHE] Product cache HIT for product ID: " + productId);
        } else {
            System.out.println("[CACHE] Product cache MISS for product ID: " + productId);
        }
        return cached;
    }
    
    /**
     * Gets all products from cache if valid.
     *
     * @return List of ProductDTO if cache is valid, null otherwise
     */
    public List<ProductDTO> getAll() {
        long currentTime = System.currentTimeMillis();
        if (productListCache != null && !productListCache.isEmpty() && 
            (currentTime - cacheTimestamp) < CACHE_TTL) {
            System.out.println("[CACHE] Product list cache HIT (" + productListCache.size() + " products)");
            return new ArrayList<>(productListCache);
        }
        System.out.println("[CACHE] Product list cache MISS or expired");
        return null;
    }
    
    /**
     * Puts a product into cache.
     * Only caches after successful DB fetch.
     *
     * @param productId Product ID
     * @param product Product model
     * @param dto ProductDTO
     */
    public void put(int productId, Product product, ProductDTO dto) {
        productCache.put(productId, dto);
        productModelCache.put(productId, product);
        System.out.println("[CACHE] Product cache LOADED for product ID: " + productId + " (" + dto.getName() + ")");
    }
    
    /**
     * Updates the product list cache.
     * Only caches after successful DB fetch.
     *
     * @param productDTOs List of ProductDTO objects
     */
    public void updateListCache(List<ProductDTO> productDTOs) {
        this.productListCache = new ArrayList<>(productDTOs);
        this.cacheTimestamp = System.currentTimeMillis();
        System.out.println("[CACHE] Product list cache LOADED (" + productDTOs.size() + " products)");
    }
    
    /**
     * Gets product ID from name using cache.
     *
     * @param productName Product name
     * @return Product ID if found, 0 otherwise
     */
    public int getProductIdFromName(String productName) {
        for (Map.Entry<Integer, Product> entry : productModelCache.entrySet()) {
            if (entry.getValue().getName().equals(productName)) {
                return entry.getKey();
            }
        }
        return 0;
    }
    
    /**
     * Invalidates all product caches.
     * Called when products are updated or deleted.
     */
    public void invalidate() {
        int cacheSize = productCache.size();
        int listSize = productListCache != null ? productListCache.size() : 0;
        productCache.clear();
        productModelCache.clear();
        productListCache.clear();
        cacheTimestamp = 0;
        System.out.println("[CACHE] Product cache INVALIDATED (" + cacheSize + " individual, " + listSize + " list entries)");
    }
}



