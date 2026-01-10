package org.example.services;

import org.example.dao.ReviewDAO;
import org.example.dto.ReviewDTO;
import org.example.models.Review;
import org.example.utils.cache.RatingCacheManager;
import org.example.utils.validators.ReviewValidator;

import java.util.List;

/**
 * Review service handling review business operations.
 * Follows Single Responsibility Principle by delegating validation and caching to dedicated classes.
 */
public class ReviewService {
    private final ReviewDAO reviewDAO;
    private final ReviewValidator validator;
    private final RatingCacheManager cacheManager;
    
    public ReviewService() {
        this.reviewDAO = new ReviewDAO();
        this.validator = new ReviewValidator();
        this.cacheManager = RatingCacheManager.getInstance();
    }
    
    /**
     * Submits a new review.
     * Validates review data and invalidates rating cache for the product.
     *
     * @param reviewDTO Review data transfer object
     * @return true if review was created successfully, false otherwise
     */
    public boolean submitReview(ReviewDTO reviewDTO) {
        // Validate review
        if (!validator.isValid(reviewDTO)) {
            System.err.println("Invalid review data");
            if (reviewDTO != null) {
                if (reviewDTO.getRating() < validator.getMinRating() || 
                    reviewDTO.getRating() > validator.getMaxRating()) {
                    System.err.println("Rating must be between " + validator.getMinRating() + 
                                     " and " + validator.getMaxRating());
                }
                if (reviewDTO.getComment() != null && 
                    reviewDTO.getComment().length() > validator.getMaxCommentLength()) {
                    System.err.println("Comment exceeds maximum length of " + 
                                     validator.getMaxCommentLength() + " characters");
                }
            }
            return false;
        }
        
        // Trim comment if needed
        if (reviewDTO.getComment() != null) {
            String trimmedComment = reviewDTO.getComment().trim();
            if (trimmedComment.length() > validator.getMaxCommentLength()) {
                trimmedComment = trimmedComment.substring(0, validator.getMaxCommentLength());
            }
            reviewDTO.setComment(trimmedComment);
        }
        
        // Create review via DAO
        boolean success = reviewDAO.createReview(reviewDTO);
        
        if (success) {
            // Invalidate rating cache for this product
            cacheManager.invalidate(reviewDTO.getProductId());
            System.out.println("Review submitted successfully for product: " + reviewDTO.getProductId());
        } else {
            System.err.println("Failed to submit review for product: " + reviewDTO.getProductId());
        }
        
        return success;
    }
    
    /**
     * Retrieves all reviews for a product.
     *
     * @param productId Product ID
     * @return List of Review objects
     */
    public List<Review> getReviewsByProduct(int productId) {
        return reviewDAO.getReviewsByProduct(productId);
    }
    
    /**
     * Computes and caches the average rating for a product.
     *
     * @param productId Product ID
     * @return Average rating as double, or 0.0 if no reviews exist
     */
    public double getAverageRating(int productId) {
        // Check cache first
        Double cachedRating = cacheManager.get(productId);
        if (cachedRating != null) {
            return cachedRating;
        }
        
        // Compute average rating via DAO
        double avgRating = reviewDAO.getAverageRating(productId);
        
        // Cache the result
        cacheManager.put(productId, avgRating);
        
        return avgRating;
    }
    
    /**
     * Clears the entire rating cache.
     */
    public void clearRatingCache() {
        cacheManager.clear();
    }
}
