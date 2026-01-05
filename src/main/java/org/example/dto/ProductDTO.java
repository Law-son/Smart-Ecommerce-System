package org.example.dto;

import java.math.BigDecimal;

/**
 * Data Transfer Object for Product operations.
 */
public class ProductDTO {
    private String name;
    private String description;
    private BigDecimal price;
    private int categoryId;
    private String imageUrl;

    public ProductDTO() {
    }

    public ProductDTO(String name, String description, BigDecimal price, int categoryId, String imageUrl) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.categoryId = categoryId;
        this.imageUrl = imageUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}

