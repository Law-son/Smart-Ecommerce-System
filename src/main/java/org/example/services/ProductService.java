package org.example.services;

import org.example.dao.ProductDAO;
import org.example.dto.ProductDTO;
import org.example.models.Product;
import org.example.utils.PerformanceMonitor;
import org.example.utils.SortingUtils;
import org.example.utils.cache.ProductCacheManager;
import org.example.utils.mappers.ProductMapper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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
     * Loads cache only after successful DB fetch.
     *
     * @return List of ProductDTO objects
     */
    public List<ProductDTO> getAllProducts() {
        return performanceMonitor.monitor("Fetch all products", () -> {
            // Check cache first
            List<ProductDTO> cachedProducts = cacheManager.getAll();
            if (cachedProducts != null) {
                return new ArrayList<>(cachedProducts);
            }
            
            // Cache miss - fetch from database
            List<Product> products = productDAO.getAllProducts();
            if (products == null || products.isEmpty()) {
                return new ArrayList<>();
            }
            
            List<ProductDTO> productDTOs = mapper.toDTOList(products);
            
            // Load cache only after successful DB fetch
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
     * Manual search implementation without streams.
     *
     * @param keyword Search keyword (case-insensitive, trimmed)
     * @return List of ProductDTO objects matching the keyword
     */
    public List<ProductDTO> searchProductsByName(String keyword) {
        return performanceMonitor.monitor("Search fallback query", () -> {
            // Trim and normalize keyword (case-insensitive)
            String searchKeyword = (keyword != null ? keyword.trim() : "").toLowerCase();
            
            if (searchKeyword.isEmpty()) {
                return getAllProducts();
            }
            
            // Cache-first search: check cache first
            List<ProductDTO> cachedResults = cacheManager.getAll();
            if (cachedResults != null) {
                // Manual search in cache (no streams)
                List<ProductDTO> filtered = new ArrayList<>();
                for (ProductDTO product : cachedResults) {
                    if (product != null && product.getName() != null) {
                        String productName = product.getName().toLowerCase();
                        if (productName.contains(searchKeyword)) {
                            filtered.add(product);
                        }
                    }
                }
                
                if (!filtered.isEmpty()) {
                    System.out.println("[CACHE] Search cache HIT - found " + filtered.size() + " products");
                    return filtered;
                } else {
                    System.out.println("[CACHE] Search cache HIT but no matches found");
                }
            } else {
                System.out.println("[CACHE] Search cache MISS - falling back to database");
            }
            
            // Cache miss or no matches - fallback to database
            List<Product> products = productDAO.searchProductsByName(keyword);
            List<ProductDTO> result = mapper.toDTOList(products);
            
            // Update cache after successful DB fetch
            if (products != null) {
                for (Product product : products) {
                    ProductDTO dto = mapper.toDTO(product);
                    cacheManager.put(product.getProductId(), product, dto);
                }
            }
            
            return result;
        });
    }
    
    /**
     * Sorts products by price (ascending or descending).
     * Uses manual Quick Sort implementation.
     *
     * @param ascending true for ascending, false for descending
     * @return Sorted list of ProductDTO objects
     */
    public List<ProductDTO> sortProductsByPrice(boolean ascending) {
        String direction = ascending ? "ascending" : "descending";
        return performanceMonitor.monitor("Sorting execution time", () -> {
            List<ProductDTO> products = new ArrayList<>(getAllProducts());
            
            if (products.size() <= 1) {
                return products;
            }
            
            // Manual Quick Sort by price
            SortingUtils.quickSortByPrice(products, ascending);
            
            return products;
        });
    }
    
    /**
     * Sorts products by name (A to Z).
     * Uses manual Merge Sort implementation.
     *
     * @return Sorted list of ProductDTO objects
     */
    public List<ProductDTO> sortProductsByName() {
        return performanceMonitor.monitor("Sorting execution time", () -> {
            List<ProductDTO> products = new ArrayList<>(getAllProducts());
            
            if (products.size() <= 1) {
                return products;
            }
            
            // Manual Merge Sort by name (A-Z)
            SortingUtils.mergeSortByName(products);
            
            return products;
        });
    }
    
    /**
     * Sorts products by average rating (highest to lowest).
     * Uses manual Quick Sort implementation.
     *
     * @return Sorted list of ProductDTO objects
     */
    public List<ProductDTO> sortProductsByRating() {
        return performanceMonitor.monitor("Sorting execution time", () -> {
            List<ProductDTO> products = new ArrayList<>(getAllProducts());
            
            if (products.size() <= 1) {
                return products;
            }
            
            // Get ratings for all products
            double[] ratings = new double[products.size()];
            for (int i = 0; i < products.size(); i++) {
                ProductDTO product = products.get(i);
                int productId = cacheManager.getProductIdFromName(product.getName());
                if (productId > 0) {
                    ratings[i] = reviewService.getAverageRating(productId);
                } else {
                    ratings[i] = 0.0;
                }
            }
            
            // Manual Quick Sort by rating (descending)
            SortingUtils.quickSortByRating(products, ratings);
            
            return products;
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
    
    /**
     * Creates a new product.
     * Invalidates cache after creation.
     *
     * @param productDTO Product data transfer object
     * @return true if product was created successfully, false otherwise
     */
    public boolean createProduct(ProductDTO productDTO) {
        if (productDTO == null) {
            System.err.println("ProductDTO cannot be null");
            return false;
        }
        
        if (productDTO.getName() == null || productDTO.getName().trim().isEmpty()) {
            System.err.println("Product name is required");
            return false;
        }
        
        if (productDTO.getPrice() == null || productDTO.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            System.err.println("Product price must be non-negative");
            return false;
        }
        
        boolean success = productDAO.createProduct(productDTO);
        
        if (success) {
            invalidateCache();
            System.out.println("Product created successfully: " + productDTO.getName());
        } else {
            System.err.println("Failed to create product: " + productDTO.getName());
        }
        
        return success;
    }
    
    /**
     * Updates an existing product.
     * Invalidates cache after update.
     *
     * @param productId Product ID
     * @param productDTO Updated product data
     * @return true if product was updated successfully, false otherwise
     */
    public boolean updateProduct(int productId, ProductDTO productDTO) {
        if (productDTO == null) {
            System.err.println("ProductDTO cannot be null");
            return false;
        }
        
        if (productDTO.getName() == null || productDTO.getName().trim().isEmpty()) {
            System.err.println("Product name is required");
            return false;
        }
        
        if (productDTO.getPrice() == null || productDTO.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            System.err.println("Product price must be non-negative");
            return false;
        }
        
        boolean success = productDAO.updateProduct(productId, productDTO);
        
        if (success) {
            invalidateCache();
            System.out.println("Product updated successfully: " + productId);
        } else {
            System.err.println("Failed to update product: " + productId);
        }
        
        return success;
    }
    
    /**
     * Deletes a product by ID.
     * Invalidates cache after deletion.
     *
     * @param productId Product ID to delete
     * @return true if product was deleted successfully, false otherwise
     */
    public boolean deleteProduct(int productId) {
        boolean success = productDAO.deleteProduct(productId);
        
        if (success) {
            invalidateCache();
            System.out.println("Product deleted successfully: " + productId);
        } else {
            System.err.println("Failed to delete product: " + productId);
        }
        
        return success;
    }
}
