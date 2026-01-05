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
        return productCache.get(productId);
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
            return new ArrayList<>(productListCache);
        }
        return null;
    }
    
    /**
     * Puts a product into cache.
     *
     * @param productId Product ID
     * @param product Product model
     * @param dto ProductDTO
     */
    public void put(int productId, Product product, ProductDTO dto) {
        productCache.put(productId, dto);
        productModelCache.put(productId, product);
    }
    
    /**
     * Updates the product list cache.
     *
     * @param productDTOs List of ProductDTO objects
     */
    public void updateListCache(List<ProductDTO> productDTOs) {
        this.productListCache = new ArrayList<>(productDTOs);
        this.cacheTimestamp = System.currentTimeMillis();
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
     */
    public void invalidate() {
        productCache.clear();
        productModelCache.clear();
        productListCache.clear();
        cacheTimestamp = 0;
    }
}

