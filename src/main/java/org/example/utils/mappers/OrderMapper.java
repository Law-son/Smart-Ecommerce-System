package org.example.utils.mappers;

import org.example.dto.OrderDTO;
import org.example.models.Order;

import java.util.ArrayList;
import java.util.List;

/**
 * Mapper for converting between Order models and OrderDTOs.
 * Follows Single Responsibility Principle - only handles Order conversions.
 */
public class OrderMapper {
    
    /**
     * Converts an Order model to OrderDTO.
     *
     * @param order Order model
     * @return OrderDTO object
     */
    public OrderDTO toDTO(Order order) {
        if (order == null) {
            return null;
        }
        
        OrderDTO dto = new OrderDTO();
        dto.setUserId(order.getUserId());
        dto.setStatus(order.getStatus());
        dto.setTotalAmount(order.getTotalAmount());
        // Items would need to be fetched separately
        dto.setItems(new ArrayList<>());
        return dto;
    }
    
    /**
     * Converts a list of Order models to OrderDTO list.
     *
     * @param orders List of Order models
     * @return List of OrderDTO objects
     */
    public List<OrderDTO> toDTOList(List<Order> orders) {
        if (orders == null) {
            return new ArrayList<>();
        }
        
        List<OrderDTO> dtos = new ArrayList<>();
        for (Order order : orders) {
            OrderDTO dto = toDTO(order);
            if (dto != null) {
                dtos.add(dto);
            }
        }
        return dtos;
    }
}




