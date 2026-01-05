package org.example.utils;

import org.example.dto.OrderItemDTO;

import java.math.BigDecimal;
import java.util.List;

/**
 * Utility class for cart calculations.
 * Follows Single Responsibility Principle - only handles cart calculations.
 */
public class CartCalculator {
    
    /**
     * Calculates the total price of all items in the cart.
     * Uses BigDecimal for precise decimal calculations.
     *
     * @param items List of OrderItemDTO objects
     * @return Total cart amount as BigDecimal
     */
    public BigDecimal calculateTotal(List<OrderItemDTO> items) {
        BigDecimal total = BigDecimal.ZERO;
        
        for (OrderItemDTO item : items) {
            BigDecimal itemTotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            total = total.add(itemTotal);
        }
        
        return total;
    }
    
    /**
     * Gets the total quantity of all items.
     *
     * @param items List of OrderItemDTO objects
     * @return Total quantity of all items
     */
    public int getTotalQuantity(List<OrderItemDTO> items) {
        return items.stream()
            .mapToInt(OrderItemDTO::getQuantity)
            .sum();
    }
}

