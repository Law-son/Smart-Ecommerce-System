package org.example.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.dto.ReviewDTO;
import org.example.services.ReviewService;
import org.example.utils.SessionManager;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for the Review Dialog screen.
 * Handles review submission.
 */
public class ReviewDialogController implements Initializable {
    
    @FXML
    private ComboBox<Integer> ratingCombo;
    
    @FXML
    private TextArea reviewComment;
    
    @FXML
    private Label reviewErrorLabel;
    
    @FXML
    private Button cancelReviewButton;
    
    @FXML
    private Button submitReviewButton;
    
    private ReviewService reviewService;
    private SessionManager sessionManager;
    private static int productIdToReview;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.reviewService = new ReviewService();
        this.sessionManager = SessionManager.getInstance();
        
        // Initialize rating combo
        ratingCombo.getItems().addAll(1, 2, 3, 4, 5);
        ratingCombo.setValue(5);
        
        // Set up event handlers
        cancelReviewButton.setOnAction(e -> handleCancel());
        submitReviewButton.setOnAction(e -> handleSubmit());
        
        // Clear error label
        reviewErrorLabel.setText("");
    }
    
    /**
     * Sets the product ID for review.
     *
     * @param productId Product ID to review
     */
    public static void setProductId(int productId) {
        productIdToReview = productId;
    }
    
    /**
     * Handles cancel button click.
     */
    private void handleCancel() {
        Stage stage = (Stage) cancelReviewButton.getScene().getWindow();
        stage.close();
    }
    
    /**
     * Handles submit review button click.
     */
    private void handleSubmit() {
        reviewErrorLabel.setText("");
        
        // Validate inputs
        Integer rating = ratingCombo.getValue();
        if (rating == null) {
            reviewErrorLabel.setText("Please select a rating");
            return;
        }
        
        if (productIdToReview <= 0) {
            reviewErrorLabel.setText("Invalid product ID");
            return;
        }
        
        // Create ReviewDTO
        ReviewDTO reviewDTO = new ReviewDTO();
        reviewDTO.setUserId(sessionManager.getCurrentUserId());
        reviewDTO.setProductId(productIdToReview);
        reviewDTO.setRating(rating);
        reviewDTO.setComment(reviewComment.getText().trim());
        
        // Submit review via ReviewService
        boolean success = reviewService.submitReview(reviewDTO);
        
        if (success) {
            // Show success message
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Review Submitted");
            alert.setHeaderText(null);
            alert.setContentText("Your review has been submitted successfully!");
            alert.showAndWait();
            
            // Close dialog - this will trigger the onCloseRequest handler in ProductDetailsController
            // which will refresh the reviews list
            Stage stage = (Stage) submitReviewButton.getScene().getWindow();
            stage.close();
        } else {
            reviewErrorLabel.setText("Failed to submit review. Please check your input and try again.");
        }
    }
}



