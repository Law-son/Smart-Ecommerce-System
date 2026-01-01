package org.example.services;

import org.example.dao.ProductDAO;
import org.example.dto.ProductDTO;
import org.example.models.Product;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Product service handling catalog operations, search, sorting, and caching.
 * Manages product caching and performance timing.
 */
public class ProductService {
    private final ProductDAO productDAO;
    private final ReviewService reviewService;
    
    // Cache for products: productId -> ProductDTO
    private final Map<Integer, ProductDTO> productCache;
    
    // Internal cache for Product models (for ID lookup)
    private final Map<Integer, Product> productModelCache;
    
    // Cache for ordered product list
    private List<ProductDTO> productListCache;
    
    // Cache timestamp to track freshness
    private long cacheTimestamp;
    private static final long CACHE_TTL = 300000; // 5 minutes in milliseconds
    
    public ProductService() {
        this.productDAO = new ProductDAO();
        this.reviewService = new ReviewService();
        this.productCache = new HashMap<>();
        this.productModelCache = new HashMap<>();
        this.productListCache = new ArrayList<>();
        this.cacheTimestamp = 0;
    }
    
    /**
     * Fetches all products with caching and performance timing.
     *
     * @return List of ProductDTO objects
     */
    public List<ProductDTO> getAllProducts() {
        long startTime = System.currentTimeMillis();
        
        // Check if cache is still valid
        long currentTime = System.currentTimeMillis();
        if (productListCache != null && !productListCache.isEmpty() && 
            (currentTime - cacheTimestamp) < CACHE_TTL) {
            long endTime = System.currentTimeMillis();
            System.out.println("[PERF] Fetch all products (from cache) executed in " + (endTime - startTime) + " ms");
            return new ArrayList<>(productListCache);
        }
        
        // Fetch from database
        List<Product> products = productDAO.getAllProducts();
        List<ProductDTO> productDTOs = convertToDTOList(products);
        
        // Update caches
        updateCaches(productDTOs);
        
        long endTime = System.currentTimeMillis();
        System.out.println("[PERF] Fetch all products executed in " + (endTime - startTime) + " ms");
        
        return new ArrayList<>(productDTOs);
    }
    
    /**
     * Searches products by name with cache-first strategy.
     *
     * @param keyword Search keyword
     * @return List of ProductDTO objects matching the keyword
     */
    public List<ProductDTO> searchProductsByName(String keyword) {
        long startTime = System.currentTimeMillis();
        
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllProducts();
        }
        
        String searchKeyword = keyword.trim().toLowerCase();
        
        // First, try to search in cache
        List<ProductDTO> cachedResults = productListCache.stream()
            .filter(p -> p.getName().toLowerCase().contains(searchKeyword))
            .collect(Collectors.toList());
        
        if (!cachedResults.isEmpty()) {
            long endTime = System.currentTimeMillis();
            System.out.println("[PERF] Search products (from cache) executed in " + (endTime - startTime) + " ms");
            return cachedResults;
        }
        
        // Cache miss - fallback to database
        List<Product> products = productDAO.searchProductsByName(keyword);
        List<ProductDTO> productDTOs = convertToDTOList(products);
        
        long endTime = System.currentTimeMillis();
        System.out.println("[PERF] Search products (DB fallback) executed in " + (endTime - startTime) + " ms");
        
        return productDTOs;
    }
    
    /**
     * Sorts products by price (ascending or descending).
     *
     * @param ascending true for ascending, false for descending
     * @return Sorted list of ProductDTO objects
     */
    public List<ProductDTO> sortProductsByPrice(boolean ascending) {
        long startTime = System.currentTimeMillis();
        
        List<ProductDTO> products = getAllProducts();
        
        products.sort((p1, p2) -> {
            BigDecimal price1 = p1.getPrice();
            BigDecimal price2 = p2.getPrice();
            int comparison = price1.compareTo(price2);
            return ascending ? comparison : -comparison;
        });
        
        long endTime = System.currentTimeMillis();
        System.out.println("[PERF] Sort products by price (" + (ascending ? "ascending" : "descending") + 
                         ") executed in " + (endTime - startTime) + " ms");
        
        return products;
    }
    
    /**
     * Sorts products by name (A to Z).
     *
     * @return Sorted list of ProductDTO objects
     */
    public List<ProductDTO> sortProductsByName() {
        long startTime = System.currentTimeMillis();
        
        List<ProductDTO> products = getAllProducts();
        
        products.sort(Comparator.comparing(ProductDTO::getName, String.CASE_INSENSITIVE_ORDER));
        
        long endTime = System.currentTimeMillis();
        System.out.println("[PERF] Sort products by name executed in " + (endTime - startTime) + " ms");
        
        return products;
    }
    
    /**
     * Sorts products by average rating (highest to lowest).
     *
     * @return Sorted list of ProductDTO objects
     */
    public List<ProductDTO> sortProductsByRating() {
        long startTime = System.currentTimeMillis();
        
        // Get products with their IDs for rating lookup
        List<Product> products = productDAO.getAllProducts();
        List<ProductDTO> productDTOs = convertToDTOList(products);
        
        // Get ratings for all products and sort
        productDTOs.sort((p1, p2) -> {
            int id1 = getProductIdFromName(p1.getName());
            int id2 = getProductIdFromName(p2.getName());
            double rating1 = reviewService.getAverageRating(id1);
            double rating2 = reviewService.getAverageRating(id2);
            return Double.compare(rating2, rating1); // Descending order
        });
        
        long endTime = System.currentTimeMillis();
        System.out.println("[PERF] Sort products by rating executed in " + (endTime - startTime) + " ms");
        
        return productDTOs;
    }
    
    /**
     * Gets a product by ID from cache or database.
     *
     * @param productId Product ID
     * @return ProductDTO if found, null otherwise
     */
    public ProductDTO getProductById(int productId) {
        // Check cache first
        if (productCache.containsKey(productId)) {
            return productCache.get(productId);
        }
        
        // Fetch from database
        Product product = productDAO.getProductById(productId);
        if (product != null) {
            ProductDTO dto = convertToDTO(product);
            productCache.put(productId, dto);
            return dto;
        }
        
        return null;
    }
    
    /**
     * Invalidates product cache.
     * Called when products are updated or deleted.
     */
    public void invalidateCache() {
        productCache.clear();
        productModelCache.clear();
        productListCache.clear();
        cacheTimestamp = 0;
        System.out.println("[CACHE] Product cache invalidated");
    }
    
    /**
     * Updates caches with product data.
     *
     * @param productDTOs List of ProductDTO objects
     */
    private void updateCaches(List<ProductDTO> productDTOs) {
        // Cache is already updated in convertToDTOList
        productListCache = new ArrayList<>(productDTOs);
        cacheTimestamp = System.currentTimeMillis();
    }
    
    /**
     * Converts a Product model to ProductDTO.
     *
     * @param product Product model
     * @return ProductDTO object
     */
    private ProductDTO convertToDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setCategoryId(product.getCategoryId());
        dto.setImageUrl(product.getImageUrl());
        return dto;
    }
    
    /**
     * Converts a list of Product models to ProductDTO list.
     *
     * @param products List of Product models
     * @return List of ProductDTO objects
     */
    private List<ProductDTO> convertToDTOList(List<Product> products) {
        List<ProductDTO> dtos = new ArrayList<>();
        for (Product product : products) {
            ProductDTO dto = convertToDTO(product);
            dtos.add(dto);
            // Store in caches with product ID
            productCache.put(product.getProductId(), dto);
            productModelCache.put(product.getProductId(), product);
        }
        return dtos;
    }
    
    /**
     * Helper method to get product ID from product name.
     *
     * @param productName Product name
     * @return Product ID if found, 0 otherwise
     */
    private int getProductIdFromName(String productName) {
        for (Map.Entry<Integer, Product> entry : productModelCache.entrySet()) {
            if (entry.getValue().getName().equals(productName)) {
                return entry.getKey();
            }
        }
        return 0;
    }
}

