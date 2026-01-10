package org.example.services;

import org.example.dao.OrderDAO;
import org.example.dto.OrderDTO;
import org.example.dto.OrderItemDTO;
import org.example.models.Order;
import org.example.utils.PerformanceMonitor;
import org.example.utils.cache.OrderCacheManager;
import org.example.utils.mappers.OrderMapper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Order service handling order creation, status updates, and caching.
 * Follows Single Responsibility Principle by delegating caching, mapping, and performance monitoring to dedicated classes.
 */
public class OrderService {
    private final OrderDAO orderDAO;
    private final InventoryService inventoryService;
    private final OrderCacheManager cacheManager;
    private final OrderMapper mapper;
    private final PerformanceMonitor performanceMonitor;
    
    public OrderService() {
        this.orderDAO = new OrderDAO();
        this.inventoryService = new InventoryService();
        this.cacheManager = OrderCacheManager.getInstance();
        this.mapper = new OrderMapper();
        this.performanceMonitor = new PerformanceMonitor();
    }
    
    /**
     * Creates a new order from cart items with stock validation.
     * Validates stock for all items before creating the order.
     *
     * @param userId User ID
     * @param cartItems List of OrderItemDTO from cart
     * @return Order ID if successful, -1 otherwise
     */
    public int createOrder(int userId, List<OrderItemDTO> cartItems) {
        if (cartItems == null || cartItems.isEmpty()) {
            System.err.println("Cannot create order with empty cart");
            return -1;
        }
        
        // Validate stock for all items
        for (OrderItemDTO item : cartItems) {
            if (!inventoryService.checkStock(item.getProductId(), item.getQuantity())) {
                int availableStock = inventoryService.getAvailableStock(item.getProductId());
                System.err.println("Order creation aborted: Insufficient stock for product " + 
                                 item.getProductId() + ". Requested " + item.getQuantity() + 
                                 ", Available " + availableStock);
                return -1;
            }
        }
        
        // Calculate total amount
        BigDecimal totalAmount = calculateOrderTotal(cartItems);
        
        // Create OrderDTO
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setUserId(userId);
        orderDTO.setStatus("PENDING");
        orderDTO.setTotalAmount(totalAmount);
        orderDTO.setItems(cartItems);
        
        // Create order via DAO (this handles order items internally)
        boolean success = orderDAO.createOrder(orderDTO);
        
        if (success) {
            // Get the created order ID by fetching the most recent order for this user
            List<Order> userOrders = orderDAO.getOrdersByUser(userId);
            if (!userOrders.isEmpty()) {
                // Get the most recent order (first in list as they're sorted by date DESC)
                Order createdOrder = userOrders.get(0);
                int orderId = createdOrder.getOrderId();
                
                // Reduce stock for all items
                for (OrderItemDTO item : cartItems) {
                    inventoryService.reduceStock(item.getProductId(), item.getQuantity());
                }
                
                // Invalidate cache
                cacheManager.invalidateUserCache(userId);
                
                System.out.println("Order created successfully with ID: " + orderId);
                return orderId;
            }
        }
        
        System.err.println("Failed to create order");
        return -1;
    }
    
    /**
     * Gets all orders for a user with caching and performance timing.
     * Loads cache only after successful DB fetch.
     *
     * @param userId User ID
     * @return List of Order objects
     */
    public List<Order> getOrdersByUser(int userId) {
        return performanceMonitor.monitor("Order fetch", () -> {
            // Check cache first
            List<OrderDTO> cachedDTOs = cacheManager.getByUser(userId);
            if (cachedDTOs != null && !cachedDTOs.isEmpty()) {
                // Cache hit - convert DTOs to Orders (simplified, would need full mapping in production)
                // For now, fetch from DB to get full Order objects
            }
            
            // Fetch from database (cache miss or need full objects)
            List<Order> orders = orderDAO.getOrdersByUser(userId);
            
            if (orders != null && !orders.isEmpty()) {
                // Load cache only after successful DB fetch
                List<OrderDTO> orderDTOs = mapper.toDTOList(orders);
                cacheManager.putByUser(userId, orderDTOs);
                
                // Also cache individual orders
                for (Order order : orders) {
                    OrderDTO dto = mapper.toDTO(order);
                    cacheManager.put(order.getOrderId(), dto);
                }
            }
            
            return orders != null ? orders : new ArrayList<>();
        });
    }
    
    /**
     * Gets all orders (Admin only) with performance timing.
     *
     * @return List of all Order objects
     */
    public List<Order> getAllOrders() {
        return performanceMonitor.monitor("getAllOrders", () -> {
            List<Order> orders = orderDAO.getAllOrders();
            
            // Cache the results
            for (Order order : orders) {
                OrderDTO dto = mapper.toDTO(order);
                cacheManager.put(order.getOrderId(), dto);
            }
            
            return orders;
        });
    }
    
    /**
     * Updates order status with cache invalidation.
     *
     * @param orderId Order ID
     * @param status New status
     * @return true if update successful, false otherwise
     */
    public boolean updateOrderStatus(int orderId, String status) {
        return performanceMonitor.monitor("Order update", () -> {
            boolean success = orderDAO.updateOrderStatus(orderId, status);
            
            if (success) {
                // Invalidate caches when order status changes
                cacheManager.invalidateOrder(orderId);
                
                // Get order to find userId for cache invalidation
                Order order = orderDAO.getOrderById(orderId);
                if (order != null) {
                    cacheManager.invalidateUserCache(order.getUserId());
                }
            }
            
            return success;
        });
    }
    
    /**
     * Gets an order by ID.
     *
     * @param orderId Order ID
     * @return Order object if found, null otherwise
     */
    public Order getOrderById(int orderId) {
        // Check cache first
        OrderDTO cachedDTO = cacheManager.getById(orderId);
        if (cachedDTO != null) {
            // For full Order object, fetch from DB
            // In a real system, you might want to cache full Order objects
        }
        
        return orderDAO.getOrderById(orderId);
    }
    
    /**
     * Calculates the total amount for an order.
     *
     * @param items List of OrderItemDTO
     * @return Total amount as BigDecimal
     */
    private BigDecimal calculateOrderTotal(List<OrderItemDTO> items) {
        BigDecimal total = BigDecimal.ZERO;
        for (OrderItemDTO item : items) {
            BigDecimal itemTotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            total = total.add(itemTotal);
        }
        return total;
    }
}
