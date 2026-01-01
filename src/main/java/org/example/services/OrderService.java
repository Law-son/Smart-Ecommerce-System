package org.example.services;

import org.example.dao.OrderDAO;
import org.example.dao.OrderItemDAO;
import org.example.dto.OrderDTO;
import org.example.dto.OrderItemDTO;
import org.example.models.Order;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Order service handling order creation, status updates, and caching.
 * Manages order business rules and performance timing.
 */
public class OrderService {
    private final OrderDAO orderDAO;
    private final OrderItemDAO orderItemDAO;
    private final InventoryService inventoryService;
    
    // Cache for recent orders: userId -> List<OrderDTO>
    private final Map<Integer, List<OrderDTO>> userOrderCache;
    
    // Cache for order lookup: orderId -> OrderDTO
    private final Map<Integer, OrderDTO> orderCache;
    
    public OrderService() {
        this.orderDAO = new OrderDAO();
        this.orderItemDAO = new OrderItemDAO();
        this.inventoryService = new InventoryService();
        this.userOrderCache = new HashMap<>();
        this.orderCache = new HashMap<>();
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
                invalidateUserOrderCache(userId);
                
                System.out.println("Order created successfully with ID: " + orderId);
                return orderId;
            }
        }
        
        System.err.println("Failed to create order");
        return -1;
    }
    
    /**
     * Gets all orders for a user with caching and performance timing.
     *
     * @param userId User ID
     * @return List of Order objects
     */
    public List<Order> getOrdersByUser(int userId) {
        long startTime = System.currentTimeMillis();
        
        // Fetch from database
        List<Order> orders = orderDAO.getOrdersByUser(userId);
        
        // Cache the results as DTOs for quick lookup
        List<OrderDTO> orderDTOs = convertOrdersToDTOs(orders);
        userOrderCache.put(userId, orderDTOs);
        
        // Also cache individual orders
        for (Order order : orders) {
            OrderDTO dto = convertOrderToDTO(order);
            orderCache.put(order.getOrderId(), dto);
        }
        
        long endTime = System.currentTimeMillis();
        System.out.println("[PERF] getOrdersByUser executed in " + (endTime - startTime) + " ms");
        
        return orders;
    }
    
    /**
     * Gets all orders (Admin only) with performance timing.
     *
     * @return List of all Order objects
     */
    public List<Order> getAllOrders() {
        long startTime = System.currentTimeMillis();
        
        List<Order> orders = orderDAO.getAllOrders();
        
        // Cache the results
        for (Order order : orders) {
            OrderDTO dto = convertOrderToDTO(order);
            orderCache.put(order.getOrderId(), dto);
        }
        
        long endTime = System.currentTimeMillis();
        System.out.println("[PERF] getAllOrders executed in " + (endTime - startTime) + " ms");
        
        return orders;
    }
    
    /**
     * Updates order status with cache invalidation.
     *
     * @param orderId Order ID
     * @param status New status
     * @return true if update successful, false otherwise
     */
    public boolean updateOrderStatus(int orderId, String status) {
        long startTime = System.currentTimeMillis();
        
        boolean success = orderDAO.updateOrderStatus(orderId, status);
        
        if (success) {
            // Invalidate caches
            invalidateOrderCache(orderId);
            
            // Get order to find userId for cache invalidation
            Order order = orderDAO.getOrderById(orderId);
            if (order != null) {
                invalidateUserOrderCache(order.getUserId());
            }
        }
        
        long endTime = System.currentTimeMillis();
        System.out.println("[PERF] updateOrderStatus executed in " + (endTime - startTime) + " ms");
        
        return success;
    }
    
    /**
     * Gets an order by ID.
     *
     * @param orderId Order ID
     * @return Order object if found, null otherwise
     */
    public Order getOrderById(int orderId) {
        // Check cache first
        if (orderCache.containsKey(orderId)) {
            OrderDTO dto = orderCache.get(orderId);
            return convertOrderDTOToOrder(dto, orderId);
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
    
    /**
     * Invalidates order cache for a specific order.
     *
     * @param orderId Order ID
     */
    private void invalidateOrderCache(int orderId) {
        if (orderCache.remove(orderId) != null) {
            System.out.println("[CACHE] Order cache invalidated for order: " + orderId);
        }
    }
    
    /**
     * Invalidates user order cache.
     *
     * @param userId User ID
     */
    private void invalidateUserOrderCache(int userId) {
        if (userOrderCache.remove(userId) != null) {
            System.out.println("[CACHE] User order cache invalidated for user: " + userId);
        }
    }
    
    /**
     * Converts Order to OrderDTO.
     */
    private OrderDTO convertOrderToDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setUserId(order.getUserId());
        dto.setStatus(order.getStatus());
        dto.setTotalAmount(order.getTotalAmount());
        // Items would need to be fetched separately
        dto.setItems(new ArrayList<>());
        return dto;
    }
    
    /**
     * Converts OrderDTO to Order.
     */
    private Order convertOrderDTOToOrder(OrderDTO dto, int orderId) {
        Order order = new Order();
        order.setOrderId(orderId);
        order.setUserId(dto.getUserId());
        order.setStatus(dto.getStatus());
        order.setTotalAmount(dto.getTotalAmount());
        return order;
    }
    
    /**
     * Converts list of Orders to OrderDTOs.
     */
    private List<OrderDTO> convertOrdersToDTOs(List<Order> orders) {
        List<OrderDTO> dtos = new ArrayList<>();
        for (Order order : orders) {
            dtos.add(convertOrderToDTO(order));
        }
        return dtos;
    }
    
    /**
     * Converts list of OrderDTOs to Orders.
     * Note: This is a simplified conversion. For full conversion, we'd need to fetch from DB.
     */
    private List<Order> convertOrderDTOsToOrders(List<OrderDTO> dtos) {
        // For cache hits, we should fetch from database to get full Order objects
        // This is a limitation of caching DTOs instead of full models
        // For now, return empty list and let the method fetch from DB
        return new ArrayList<>();
    }
}

