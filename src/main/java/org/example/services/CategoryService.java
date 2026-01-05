package org.example.services;

import org.example.dao.CategoryDAO;
import org.example.dto.CategoryDTO;
import org.example.models.Category;
import org.example.utils.validators.CategoryValidator;

import java.util.List;

/**
 * Category service handling category business operations.
 * Follows Single Responsibility Principle by delegating validation to CategoryValidator.
 */
public class CategoryService {
    private final CategoryDAO categoryDAO;
    private final CategoryValidator validator;
    
    public CategoryService() {
        this.categoryDAO = new CategoryDAO();
        this.validator = new CategoryValidator();
    }
    
    /**
     * Creates a new category.
     *
     * @param categoryDTO Category data transfer object
     * @return true if category was created successfully, false otherwise
     */
    public boolean createCategory(CategoryDTO categoryDTO) {
        if (!validator.isValid(categoryDTO)) {
            System.err.println("Invalid category data");
            if (categoryDTO != null && categoryDTO.getCategoryName() != null) {
                if (categoryDTO.getCategoryName().length() > validator.getMaxCategoryNameLength()) {
                    System.err.println("Category name exceeds maximum length of " + 
                                     validator.getMaxCategoryNameLength() + " characters");
                } else if (categoryDTO.getCategoryName().trim().isEmpty()) {
                    System.err.println("Category name cannot be null or empty");
                }
            } else {
                System.err.println("Category DTO cannot be null");
            }
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
        if (!validator.isValid(categoryDTO)) {
            System.err.println("Invalid category data");
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
