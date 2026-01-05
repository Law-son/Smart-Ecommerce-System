package org.example.services;

import org.example.dao.ProductDAO;
import org.example.dto.ProductDTO;
import org.example.models.Product;
import org.example.utils.PerformanceMonitor;
import org.example.utils.cache.ProductCacheManager;
import org.example.utils.mappers.ProductMapper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Product service handling catalog operations, search, and sorting.
 * Follows Single Responsibility Principle by delegating caching, mapping, and performance monitoring to dedicated classes.
 */
public class ProductService {
    private final ProductDAO productDAO;
    private final ReviewService reviewService;
    private final ProductCacheManager cacheManager;
    private final ProductMapper mapper;
    private final PerformanceMonitor performanceMonitor;
    
    public ProductService() {
        this.productDAO = new ProductDAO();
        this.reviewService = new ReviewService();
        this.cacheManager = new ProductCacheManager();
        this.mapper = new ProductMapper();
        this.performanceMonitor = new PerformanceMonitor();
    }
    
    /**
     * Fetches all products with caching and performance timing.
     *
     * @return List of ProductDTO objects
     */
    public List<ProductDTO> getAllProducts() {
        return performanceMonitor.monitor("Fetch all products", () -> {
            // Check cache first
            List<ProductDTO> cachedProducts = cacheManager.getAll();
            if (cachedProducts != null) {
                System.out.println("[PERF] Fetch all products (from cache)");
                return new ArrayList<>(cachedProducts);
            }
            
            // Fetch from database
            List<Product> products = productDAO.getAllProducts();
            List<ProductDTO> productDTOs = mapper.toDTOList(products);
            
            // Update caches
            for (Product product : products) {
                ProductDTO dto = mapper.toDTO(product);
                cacheManager.put(product.getProductId(), product, dto);
            }
            cacheManager.updateListCache(productDTOs);
            
            return new ArrayList<>(productDTOs);
        });
    }
    
    /**
     * Searches products by name with cache-first strategy.
     *
     * @param keyword Search keyword
     * @return List of ProductDTO objects matching the keyword
     */
    public List<ProductDTO> searchProductsByName(String keyword) {
        return performanceMonitor.monitor("Search products", () -> {
            if (keyword == null || keyword.trim().isEmpty()) {
                return getAllProducts();
            }
            
            String searchKeyword = keyword.trim().toLowerCase();
            
            // First, try to search in cache
            List<ProductDTO> cachedResults = cacheManager.getAll();
            if (cachedResults != null) {
                List<ProductDTO> filtered = cachedResults.stream()
                    .filter(p -> p.getName().toLowerCase().contains(searchKeyword))
                    .collect(Collectors.toList());
                if (!filtered.isEmpty()) {
                    System.out.println("[PERF] Search products (from cache)");
                    return filtered;
                }
            }
            
            // Cache miss - fallback to database
            List<Product> products = productDAO.searchProductsByName(keyword);
            return mapper.toDTOList(products);
        });
    }
    
    /**
     * Sorts products by price (ascending or descending).
     *
     * @param ascending true for ascending, false for descending
     * @return Sorted list of ProductDTO objects
     */
    public List<ProductDTO> sortProductsByPrice(boolean ascending) {
        String direction = ascending ? "ascending" : "descending";
        return performanceMonitor.monitor("Sort products by price (" + direction + ")", () -> {
            List<ProductDTO> products = getAllProducts();
            
            products.sort((p1, p2) -> {
                BigDecimal price1 = p1.getPrice();
                BigDecimal price2 = p2.getPrice();
                int comparison = price1.compareTo(price2);
                return ascending ? comparison : -comparison;
            });
            
            return products;
        });
    }
    
    /**
     * Sorts products by name (A to Z).
     *
     * @return Sorted list of ProductDTO objects
     */
    public List<ProductDTO> sortProductsByName() {
        return performanceMonitor.monitor("Sort products by name", () -> {
            List<ProductDTO> products = getAllProducts();
            products.sort(Comparator.comparing(ProductDTO::getName, String.CASE_INSENSITIVE_ORDER));
            return products;
        });
    }
    
    /**
     * Sorts products by average rating (highest to lowest).
     *
     * @return Sorted list of ProductDTO objects
     */
    public List<ProductDTO> sortProductsByRating() {
        return performanceMonitor.monitor("Sort products by rating", () -> {
            // Get products with their IDs for rating lookup
            List<Product> products = productDAO.getAllProducts();
            List<ProductDTO> productDTOs = mapper.toDTOList(products);
            
            // Update cache
            for (Product product : products) {
                ProductDTO dto = mapper.toDTO(product);
                cacheManager.put(product.getProductId(), product, dto);
            }
            
            // Get ratings for all products and sort
            productDTOs.sort((p1, p2) -> {
                int id1 = cacheManager.getProductIdFromName(p1.getName());
                int id2 = cacheManager.getProductIdFromName(p2.getName());
                double rating1 = reviewService.getAverageRating(id1);
                double rating2 = reviewService.getAverageRating(id2);
                return Double.compare(rating2, rating1); // Descending order
            });
            
            return productDTOs;
        });
    }
    
    /**
     * Gets a product by ID from cache or database.
     *
     * @param productId Product ID
     * @return ProductDTO if found, null otherwise
     */
    public ProductDTO getProductById(int productId) {
        // Check cache first
        ProductDTO cached = cacheManager.getById(productId);
        if (cached != null) {
            return cached;
        }
        
        // Fetch from database
        Product product = productDAO.getProductById(productId);
        if (product != null) {
            ProductDTO dto = mapper.toDTO(product);
            cacheManager.put(productId, product, dto);
            return dto;
        }
        
        return null;
    }
    
    /**
     * Invalidates product cache.
     * Called when products are updated or deleted.
     */
    public void invalidateCache() {
        cacheManager.invalidate();
        System.out.println("[CACHE] Product cache invalidated");
    }
}
