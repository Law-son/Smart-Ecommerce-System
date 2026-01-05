package org.example.dto;

/**
 * Data Transfer Object for Inventory operations.
 */
public class InventoryDTO {
    private int productId;
    private int quantity;

    public InventoryDTO() {
    }

    public InventoryDTO(int productId, int quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

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
}

