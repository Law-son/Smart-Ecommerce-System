package org.example.utils.cache;

import org.example.dto.OrderDTO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Cache manager for order data.
 * Follows Single Responsibility Principle - only handles order caching.
 */
public class OrderCacheManager {
    
    // Cache for recent orders: userId -> List<OrderDTO>
    private final Map<Integer, List<OrderDTO>> userOrderCache;
    
    // Cache for order lookup: orderId -> OrderDTO
    private final Map<Integer, OrderDTO> orderCache;
    
    public OrderCacheManager() {
        this.userOrderCache = new HashMap<>();
        this.orderCache = new HashMap<>();
    }
    
    /**
     * Gets orders for a user from cache.
     *
     * @param userId User ID
     * @return List of OrderDTO if found in cache, null otherwise
     */
    public List<OrderDTO> getByUser(int userId) {
        return userOrderCache.get(userId);
    }
    
    /**
     * Gets an order by ID from cache.
     *
     * @param orderId Order ID
     * @return OrderDTO if found in cache, null otherwise
     */
    public OrderDTO getById(int orderId) {
        return orderCache.get(orderId);
    }
    
    /**
     * Puts orders for a user into cache.
     *
     * @param userId User ID
     * @param orderDTOs List of OrderDTO objects
     */
    public void putByUser(int userId, List<OrderDTO> orderDTOs) {
        userOrderCache.put(userId, new ArrayList<>(orderDTOs));
    }
    
    /**
     * Puts an order into cache.
     *
     * @param orderId Order ID
     * @param orderDTO OrderDTO object
     */
    public void put(int orderId, OrderDTO orderDTO) {
        orderCache.put(orderId, orderDTO);
    }
    
    /**
     * Invalidates cache for a specific order.
     *
     * @param orderId Order ID
     */
    public void invalidateOrder(int orderId) {
        if (orderCache.remove(orderId) != null) {
            System.out.println("[CACHE] Order cache invalidated for order: " + orderId);
        }
    }
    
    /**
     * Invalidates user order cache.
     *
     * @param userId User ID
     */
    public void invalidateUserCache(int userId) {
        if (userOrderCache.remove(userId) != null) {
            System.out.println("[CACHE] User order cache invalidated for user: " + userId);
        }
    }
}

