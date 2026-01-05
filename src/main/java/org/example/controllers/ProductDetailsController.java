package org.example.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.Scene;
import org.example.dao.ProductDAO;
import org.example.dto.ProductDTO;
import org.example.dto.ReviewDTO;
import org.example.models.Product;
import org.example.models.Review;
import org.example.services.CartService;
import org.example.services.ProductService;
import org.example.services.ReviewService;
import org.example.utils.NavigationHelper;
import org.example.utils.ProductStateManager;
import org.example.utils.SessionManager;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller for the Product Details screen.
 * Handles product display, reviews, and adding to cart.
 */
public class ProductDetailsController implements Initializable {
    
    @FXML
    private Label productName;
    
    @FXML
    private Label productPrice;
    
    @FXML
    private Label productDesc;
    
    @FXML
    private ImageView productImage;
    
    @FXML
    private ListView<String> reviewsList;
    
    @FXML
    private Label avgRatingLabel;
    
    @FXML
    private Button addReviewButton;
    
    @FXML
    private Button addToCartButton;
    
    private ProductService productService;
    private ReviewService reviewService;
    private CartService cartService;
    private SessionManager sessionManager;
    private ProductDAO productDAO;
    private int currentProductId;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.productService = new ProductService();
        this.reviewService = new ReviewService();
        this.cartService = new CartService();
        this.sessionManager = SessionManager.getInstance();
        this.productDAO = new ProductDAO();
        
        // Check if user is logged in
        if (!sessionManager.isLoggedIn()) {
            navigateToLogin();
            return;
        }
        
        // Get product to view from state manager
        ProductDTO productDTO = ProductStateManager.getAndClearProductToView();
        if (productDTO == null) {
            // If no product set, navigate back to catalog
            navigateToCatalog();
            return;
        }
        
        // Get full product with ID from database
        // We need to find product by name since DTO doesn't have ID
        List<Product> products = productDAO.getAllProducts();
        Product product = products.stream()
            .filter(p -> p.getName().equals(productDTO.getName()) && 
                        p.getPrice().equals(productDTO.getPrice()))
            .findFirst()
            .orElse(null);
        
        if (product == null) {
            // Try to find by name only if price match fails
            product = products.stream()
                .filter(p -> p.getName().equals(productDTO.getName()))
                .findFirst()
                .orElse(null);
        }
        
        if (product == null) {
            navigateToCatalog();
            return;
        }
        
        currentProductId = product.getProductId();
        
        // Load product details
        loadProductDetails(product);
        
        // Load reviews
        loadReviews();
        
        // Set up event handlers
        addReviewButton.setOnAction(e -> handleAddReview());
        addToCartButton.setOnAction(e -> handleAddToCart());
    }
    
    /**
     * Loads and displays product details.
     */
    private void loadProductDetails(Product product) {
        productName.setText(product.getName());
        productPrice.setText("$" + product.getPrice());
        productDesc.setText(product.getDescription());
        
        // Load image if URL is provided
        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            try {
                Image image = new Image(product.getImageUrl());
                productImage.setImage(image);
            } catch (Exception e) {
                System.err.println("Error loading product image: " + e.getMessage());
            }
        }
        
        // Load average rating
        double avgRating = reviewService.getAverageRating(product.getProductId());
        avgRatingLabel.setText(String.format("Rating: %.1f / 5.0", avgRating));
    }
    
    /**
     * Loads and displays reviews for the product.
     */
    private void loadReviews() {
        List<Review> reviews = reviewService.getReviewsByProduct(currentProductId);
        reviewsList.getItems().clear();
        
        for (Review review : reviews) {
            String reviewText = String.format("Rating: %d/5 - %s", 
                review.getRating(), 
                review.getComment() != null ? review.getComment() : "No comment");
            reviewsList.getItems().add(reviewText);
        }
        
        if (reviews.isEmpty()) {
            reviewsList.getItems().add("No reviews yet. Be the first to review!");
        }
    }
    
    /**
     * Handles add review button click.
     */
    private void handleAddReview() {
        Scene scene = addReviewButton.getScene();
        if (scene != null) {
            // Store product ID for review dialog
            ReviewDialogController.setProductId(currentProductId);
            Stage dialogStage = NavigationHelper.openDialog("ReviewDialog.fxml", NavigationHelper.getStage(scene));
            if (dialogStage != null) {
                // Reload reviews after dialog closes
                dialogStage.setOnCloseRequest(e -> loadReviews());
            }
        }
    }
    
    /**
     * Handles add to cart button click.
     */
    private void handleAddToCart() {
        boolean success = cartService.addToCart(currentProductId, 1);
        
        if (success) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText(null);
            alert.setContentText("Product added to cart successfully!");
            alert.showAndWait();
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Failed to add product to cart. Check stock availability.");
            alert.showAndWait();
        }
    }
    
    /**
     * Navigates to Catalog screen.
     */
    private void navigateToCatalog() {
        Scene scene = addToCartButton.getScene();
        if (scene != null) {
            NavigationHelper.navigateTo("Catalog.fxml", NavigationHelper.getStage(scene));
        }
    }
    
    /**
     * Navigates to Login screen.
     */
    private void navigateToLogin() {
        Scene scene = addToCartButton.getScene();
        if (scene != null) {
            NavigationHelper.navigateTo("Login.fxml", NavigationHelper.getStage(scene));
        }
    }
}

