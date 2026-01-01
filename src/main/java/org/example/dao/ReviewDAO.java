package org.example.dao;

import org.example.configs.DatabaseConfig;
import org.example.dto.ReviewDTO;
import org.example.models.Review;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Review operations.
 * Handles all database interactions for Review entities.
 */
public class ReviewDAO {

    /**
     * Creates a new review.
     *
     * @param dto The review data transfer object
     * @return true if review was created successfully, false otherwise
     */
    public boolean createReview(ReviewDTO dto) {
        String sql = "INSERT INTO reviews (user_id, product_id, rating, comment) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, dto.getUserId());
            pstmt.setInt(2, dto.getProductId());
            pstmt.setInt(3, dto.getRating());
            pstmt.setString(4, dto.getComment());
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error creating review: " + e.getMessage());
            if (e.getSQLState().equals("23503")) { // Foreign key constraint violation
                System.err.println("User ID or Product ID does not exist");
            }
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Retrieves all reviews for a specific product.
     *
     * @param productId The product ID
     * @return List of Review objects for the product
     */
    public List<Review> getReviewsByProduct(int productId) {
        String sql = "SELECT review_id, user_id, product_id, rating, comment, created_at FROM reviews WHERE product_id = ? ORDER BY created_at DESC";
        List<Review> reviews = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, productId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Review review = new Review();
                    review.setReviewId(rs.getInt("review_id"));
                    review.setUserId(rs.getInt("user_id"));
                    review.setProductId(rs.getInt("product_id"));
                    review.setRating(rs.getInt("rating"));
                    review.setComment(rs.getString("comment"));
                    Timestamp createdAt = rs.getTimestamp("created_at");
                    if (createdAt != null) {
                        review.setCreatedAt(createdAt.toLocalDateTime());
                    }
                    reviews.add(review);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving reviews by product: " + e.getMessage());
            e.printStackTrace();
        }
        
        return reviews;
    }

    /**
     * Calculates the average rating for a product.
     *
     * @param productId The product ID
     * @return Average rating as a double, or 0.0 if no reviews exist
     */
    public double getAverageRating(int productId) {
        String sql = "SELECT AVG(rating) as avg_rating FROM reviews WHERE product_id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, productId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    double avgRating = rs.getDouble("avg_rating");
                    return rs.wasNull() ? 0.0 : avgRating;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error calculating average rating: " + e.getMessage());
            e.printStackTrace();
        }
        
        return 0.0;
    }
}

