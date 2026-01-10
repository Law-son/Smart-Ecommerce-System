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
        List<OrderDTO> orders = userOrderCache.get(userId);
        if (orders != null) {
            System.out.println("[CACHE] Order cache HIT for user: " + userId + " (" + orders.size() + " orders)");
        } else {
            System.out.println("[CACHE] Order cache MISS for user: " + userId);
        }
        return orders;
    }
    
    /**
     * Gets an order by ID from cache.
     *
     * @param orderId Order ID
     * @return OrderDTO if found in cache, null otherwise
     */
    public OrderDTO getById(int orderId) {
        OrderDTO order = orderCache.get(orderId);
        if (order != null) {
            System.out.println("[CACHE] Order cache HIT for order ID: " + orderId);
        } else {
            System.out.println("[CACHE] Order cache MISS for order ID: " + orderId);
        }
        return order;
    }
    
    /**
     * Puts orders for a user into cache.
     * Only caches after successful DB fetch.
     *
     * @param userId User ID
     * @param orderDTOs List of OrderDTO objects
     */
    public void putByUser(int userId, List<OrderDTO> orderDTOs) {
        userOrderCache.put(userId, new ArrayList<>(orderDTOs));
        System.out.println("[CACHE] Order cache LOADED for user: " + userId + " (" + orderDTOs.size() + " orders)");
    }
    
    /**
     * Puts an order into cache.
     * Only caches after successful DB fetch.
     *
     * @param orderId Order ID
     * @param orderDTO OrderDTO object
     */
    public void put(int orderId, OrderDTO orderDTO) {
        orderCache.put(orderId, orderDTO);
        System.out.println("[CACHE] Order cache LOADED for order ID: " + orderId);
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



