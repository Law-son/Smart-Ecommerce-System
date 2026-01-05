package org.example.services;

import org.example.dao.ReviewDAO;
import org.example.dto.ReviewDTO;
import org.example.models.Review;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Review service handling review validation, submission, and rating computation.
 * Manages review business rules and caches average ratings.
 */
public class ReviewService {
    private final ReviewDAO reviewDAO;
    
    // Cache for average ratings: productId -> averageRating
    private final Map<Integer, Double> ratingCache;
    
    // Maximum comment length
    private static final int MAX_COMMENT_LENGTH = 500;
    
    // Rating bounds
    private static final int MIN_RATING = 1;
    private static final int MAX_RATING = 5;
    
    public ReviewService() {
        this.reviewDAO = new ReviewDAO();
        this.ratingCache = new HashMap<>();
    }
    
    /**
     * Validates review data before submission.
     *
     * @param reviewDTO Review data transfer object
     * @return true if review is valid, false otherwise
     */
    public boolean validateReview(ReviewDTO reviewDTO) {
        // Validate rating
        if (reviewDTO.getRating() < MIN_RATING || reviewDTO.getRating() > MAX_RATING) {
            System.err.println("Rating must be between " + MIN_RATING + " and " + MAX_RATING);
            return false;
        }
        
        // Validate comment length
        if (reviewDTO.getComment() != null && reviewDTO.getComment().length() > MAX_COMMENT_LENGTH) {
            System.err.println("Comment exceeds maximum length of " + MAX_COMMENT_LENGTH + " characters");
            return false;
        }
        
        return true;
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
        if (!validateReview(reviewDTO)) {
            return false;
        }
        
        // Trim comment if needed
        if (reviewDTO.getComment() != null) {
            String trimmedComment = reviewDTO.getComment().trim();
            if (trimmedComment.length() > MAX_COMMENT_LENGTH) {
                trimmedComment = trimmedComment.substring(0, MAX_COMMENT_LENGTH);
            }
            reviewDTO.setComment(trimmedComment);
        }
        
        // Create review via DAO
        boolean success = reviewDAO.createReview(reviewDTO);
        
        if (success) {
            // Invalidate rating cache for this product
            invalidateRatingCache(reviewDTO.getProductId());
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
        if (ratingCache.containsKey(productId)) {
            return ratingCache.get(productId);
        }
        
        // Compute average rating via DAO
        double avgRating = reviewDAO.getAverageRating(productId);
        
        // Cache the result
        ratingCache.put(productId, avgRating);
        
        return avgRating;
    }
    
    /**
     * Invalidates the rating cache for a specific product.
     *
     * @param productId Product ID
     */
    public void invalidateRatingCache(int productId) {
        if (ratingCache.remove(productId) != null) {
            System.out.println("[CACHE] Rating cache invalidated for product: " + productId);
        }
    }
    
    /**
     * Clears the entire rating cache.
     */
    public void clearRatingCache() {
        ratingCache.clear();
        System.out.println("[CACHE] Rating cache cleared");
    }
}





