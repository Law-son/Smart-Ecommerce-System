package org.example.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Transfer Object for Order operations.
 * Contains a list of OrderItemDTO for order creation.
 */
public class OrderDTO {
    private int userId;
    private String status;
    private BigDecimal totalAmount;
    private List<OrderItemDTO> items;

    public OrderDTO() {
        this.items = new ArrayList<>();
    }

    public OrderDTO(int userId, String status, BigDecimal totalAmount, List<OrderItemDTO> items) {
        this.userId = userId;
        this.status = status;
        this.totalAmount = totalAmount;
        this.items = items != null ? items : new ArrayList<>();
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public List<OrderItemDTO> getItems() {
        return items;
    }

    public void setItems(List<OrderItemDTO> items) {
        this.items = items != null ? items : new ArrayList<>();
    }
}

