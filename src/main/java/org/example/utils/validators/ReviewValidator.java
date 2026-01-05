package org.example.utils.validators;

import org.example.dto.ReviewDTO;

/**
 * Validator for review data.
 * Follows Single Responsibility Principle - only handles review validation.
 */
public class ReviewValidator {
    
    private static final int MIN_RATING = 1;
    private static final int MAX_RATING = 5;
    private static final int MAX_COMMENT_LENGTH = 500;
    
    /**
     * Validates review data.
     *
     * @param reviewDTO Review data transfer object
     * @return true if review is valid, false otherwise
     */
    public boolean isValid(ReviewDTO reviewDTO) {
        if (reviewDTO == null) {
            return false;
        }
        
        // Validate rating
        if (reviewDTO.getRating() < MIN_RATING || reviewDTO.getRating() > MAX_RATING) {
            return false;
        }
        
        // Validate comment length
        if (reviewDTO.getComment() != null && reviewDTO.getComment().length() > MAX_COMMENT_LENGTH) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Gets the minimum rating value.
     *
     * @return Minimum rating
     */
    public int getMinRating() {
        return MIN_RATING;
    }
    
    /**
     * Gets the maximum rating value.
     *
     * @return Maximum rating
     */
    public int getMaxRating() {
        return MAX_RATING;
    }
    
    /**
     * Gets the maximum comment length.
     *
     * @return Maximum comment length
     */
    public int getMaxCommentLength() {
        return MAX_COMMENT_LENGTH;
    }
}

