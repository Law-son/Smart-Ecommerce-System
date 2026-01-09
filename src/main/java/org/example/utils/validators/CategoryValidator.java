package org.example.utils.validators;

import org.example.dto.CategoryDTO;

/**
 * Validator for category data.
 * Follows Single Responsibility Principle - only handles category validation.
 */
public class CategoryValidator {
    
    private static final int MAX_CATEGORY_NAME_LENGTH = 80;
    
    /**
     * Validates category data.
     *
     * @param categoryDTO Category data transfer object
     * @return true if category is valid, false otherwise
     */
    public boolean isValid(CategoryDTO categoryDTO) {
        if (categoryDTO == null) {
            return false;
        }
        
        String categoryName = categoryDTO.getCategoryName();
        if (categoryName == null || categoryName.trim().isEmpty()) {
            return false;
        }
        
        if (categoryName.length() > MAX_CATEGORY_NAME_LENGTH) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Gets the maximum category name length.
     *
     * @return Maximum category name length
     */
    public int getMaxCategoryNameLength() {
        return MAX_CATEGORY_NAME_LENGTH;
    }
}



