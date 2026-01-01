package org.example.dto;

import java.math.BigDecimal;

/**
 * Data Transfer Object for OrderItem operations.
 */
public class OrderItemDTO {
    private int productId;
    private int quantity;
    private BigDecimal unitPrice;

    // Default constructor
    public OrderItemDTO() {
    }

    // Constructor
    public OrderItemDTO(int productId, int quantity, BigDecimal unitPrice) {
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    // Getters and Setters
    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }
}

