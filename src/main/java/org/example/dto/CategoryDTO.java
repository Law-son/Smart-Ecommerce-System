package org.example.dto;

/**
 * Data Transfer Object for Category operations.
 */
public class CategoryDTO {
    private String categoryName;

    // Default constructor
    public CategoryDTO() {
    }

    // Constructor
    public CategoryDTO(String categoryName) {
        this.categoryName = categoryName;
    }

    // Getters and Setters
    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
}

