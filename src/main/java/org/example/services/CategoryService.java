package org.example.services;

import org.example.dao.CategoryDAO;
import org.example.dto.CategoryDTO;
import org.example.models.Category;

import java.util.List;

/**
 * Category service handling category validation and business rules.
 */
public class CategoryService {
    private final CategoryDAO categoryDAO;
    
    // Maximum category name length
    private static final int MAX_CATEGORY_NAME_LENGTH = 80;
    
    public CategoryService() {
        this.categoryDAO = new CategoryDAO();
    }
    
    /**
     * Validates category data.
     *
     * @param categoryDTO Category data transfer object
     * @return true if category is valid, false otherwise
     */
    public boolean validateCategory(CategoryDTO categoryDTO) {
        if (categoryDTO == null) {
            System.err.println("Category DTO cannot be null");
            return false;
        }
        
        String categoryName = categoryDTO.getCategoryName();
        if (categoryName == null || categoryName.trim().isEmpty()) {
            System.err.println("Category name cannot be null or empty");
            return false;
        }
        
        if (categoryName.length() > MAX_CATEGORY_NAME_LENGTH) {
            System.err.println("Category name exceeds maximum length of " + MAX_CATEGORY_NAME_LENGTH + " characters");
            return false;
        }
        
        return true;
    }
    
    /**
     * Creates a new category.
     *
     * @param categoryDTO Category data transfer object
     * @return true if category was created successfully, false otherwise
     */
    public boolean createCategory(CategoryDTO categoryDTO) {
        if (!validateCategory(categoryDTO)) {
            return false;
        }
        
        // Trim and normalize category name
        String trimmedName = categoryDTO.getCategoryName().trim();
        categoryDTO.setCategoryName(trimmedName);
        
        return categoryDAO.createCategory(categoryDTO);
    }
    
    /**
     * Retrieves a category by ID.
     *
     * @param id Category ID
     * @return Category object if found, null otherwise
     */
    public Category getCategoryById(int id) {
        return categoryDAO.getCategoryById(id);
    }
    
    /**
     * Retrieves all categories.
     *
     * @return List of all Category objects
     */
    public List<Category> getAllCategories() {
        return categoryDAO.getAllCategories();
    }
    
    /**
     * Updates an existing category.
     *
     * @param id Category ID
     * @param categoryDTO Updated category data
     * @return true if update successful, false otherwise
     */
    public boolean updateCategory(int id, CategoryDTO categoryDTO) {
        if (!validateCategory(categoryDTO)) {
            return false;
        }
        
        // Trim and normalize category name
        String trimmedName = categoryDTO.getCategoryName().trim();
        categoryDTO.setCategoryName(trimmedName);
        
        return categoryDAO.updateCategory(id, categoryDTO);
    }
    
    /**
     * Deletes a category.
     *
     * @param id Category ID
     * @return true if deletion successful, false otherwise
     */
    public boolean deleteCategory(int id) {
        return categoryDAO.deleteCategory(id);
    }
}





