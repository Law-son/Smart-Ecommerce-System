package org.example.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.Scene;
import org.example.dto.ProductDTO;
import org.example.services.ProductService;
import org.example.utils.NavigationHelper;
import org.example.utils.ProductStateManager;
import org.example.utils.SessionManager;

import java.math.BigDecimal;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller for the Catalog screen.
 * Handles product browsing, search, and sorting.
 */
public class CatalogController implements Initializable {
    
    @FXML
    private TextField searchField;
    
    @FXML
    private ComboBox<String> sortCombo;
    
    @FXML
    private GridPane productList;
    
    @FXML
    private Button ordersButton;
    
    @FXML
    private Button cartButton;
    
    @FXML
    private Button logoutButton;
    
    @FXML
    private Button refreshButton;
    
    @FXML
    private Label perfLabel;
    
    @FXML
    private ProgressIndicator loadingIndicator;
    
    private ProductService productService;
    private SessionManager sessionManager;
    private ProductDTO selectedProduct;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.productService = new ProductService();
        this.sessionManager = SessionManager.getInstance();
        
        // Check if user is logged in
        if (!sessionManager.isLoggedIn()) {
            navigateToLogin();
            return;
        }
        
        // Initialize sort combo
        sortCombo.getItems().addAll("Price ↑", "Price ↓", "Name A-Z", "Rating ↓");
        sortCombo.setValue("Price ↑");
        
        // Set up event handlers
        searchField.setOnAction(e -> handleSearch());
        sortCombo.setOnAction(e -> handleSort());
        ordersButton.setOnAction(e -> navigateToOrders());
        cartButton.setOnAction(e -> navigateToCart());
        refreshButton.setOnAction(e -> loadProducts());
        logoutButton.setOnAction(e -> handleLogout());
        
        // Load products
        loadProducts();
    }
    
    /**
     * Loads all products and displays them.
     */
    private void loadProducts() {
        try {
            showLoading(true);
            List<ProductDTO> products = productService.getAllProducts();
            
            if (products == null) {
                products = new java.util.ArrayList<>();
                perfLabel.setText("Error: Failed to load products");
                showLoading(false);
                return;
            }
            
            // Performance timing is logged by ProductService via PerformanceMonitor
            perfLabel.setText("Loaded " + products.size() + " products");
            
            displayProducts(products);
            showLoading(false);
        } catch (Exception e) {
            System.err.println("Error loading products: " + e.getMessage());
            e.printStackTrace();
            perfLabel.setText("Error loading products. Please try again.");
            displayProducts(new java.util.ArrayList<>());
            showLoading(false);
        }
    }
    
    /**
     * Shows or hides the loading indicator.
     */
    private void showLoading(boolean show) {
        if (loadingIndicator != null) {
            loadingIndicator.setVisible(show);
            loadingIndicator.setManaged(show);
        }
    }
    
    /**
     * Navigates to orders screen.
     */
    private void navigateToOrders() {
        Scene scene = ordersButton.getScene();
        if (scene != null) {
            NavigationHelper.navigateTo("Orders.fxml", NavigationHelper.getStage(scene));
        }
    }
    
    /**
     * Navigates to cart screen.
     */
    private void navigateToCart() {
        Scene scene = cartButton.getScene();
        if (scene != null) {
            NavigationHelper.navigateTo("Cart.fxml", NavigationHelper.getStage(scene));
        }
    }
    
    /**
     * Handles logout button click.
     */
    private void handleLogout() {
        try {
            sessionManager.clearSession();
            Scene scene = logoutButton.getScene();
            if (scene != null) {
                NavigationHelper.navigateTo("Login.fxml", NavigationHelper.getStage(scene));
            }
        } catch (Exception e) {
            System.err.println("Error during logout: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Handles search field input.
     */
    private void handleSearch() {
        try {
            String keyword = searchField.getText().trim();
            
            List<ProductDTO> products;
            
            if (keyword.isEmpty()) {
                products = productService.getAllProducts();
            } else {
                products = productService.searchProductsByName(keyword);
            }
            
            if (products == null) {
                products = new java.util.ArrayList<>();
                perfLabel.setText("Error: Search failed");
                displayProducts(products);
                return;
            }
            
            // Performance timing is logged by ProductService via PerformanceMonitor
            perfLabel.setText("Found " + products.size() + " products");
            
            displayProducts(products);
        } catch (Exception e) {
            System.err.println("Error during search: " + e.getMessage());
            e.printStackTrace();
            perfLabel.setText("Error during search. Please try again.");
            displayProducts(new java.util.ArrayList<>());
        }
    }
    
    /**
     * Handles sort combo selection.
     */
    private void handleSort() {
        try {
            String sortOption = sortCombo.getValue();
            if (sortOption == null) return;
            
            List<ProductDTO> products;
            
            switch (sortOption) {
                case "Price ↑":
                    products = productService.sortProductsByPrice(true);
                    break;
                case "Price ↓":
                    products = productService.sortProductsByPrice(false);
                    break;
                case "Name A-Z":
                    products = productService.sortProductsByName();
                    break;
                case "Rating ↓":
                    products = productService.sortProductsByRating();
                    break;
                default:
                    products = productService.getAllProducts();
            }
            
            if (products == null) {
                products = new java.util.ArrayList<>();
                perfLabel.setText("Error: Sort failed");
                displayProducts(products);
                return;
            }
            
            // Performance timing is logged by ProductService via PerformanceMonitor
            perfLabel.setText("Sorted " + products.size() + " products");
            
            displayProducts(products);
        } catch (Exception e) {
            System.err.println("Error during sort: " + e.getMessage());
            e.printStackTrace();
            perfLabel.setText("Error during sort. Please try again.");
            displayProducts(new java.util.ArrayList<>());
        }
    }
    
    /**
     * Displays products in the GridPane.
     */
    private void displayProducts(List<ProductDTO> products) {
        productList.getChildren().clear();
        
        int col = 0;
        int row = 0;
        int maxCols = 3;
        
        for (ProductDTO product : products) {
            VBox productCard = createProductCard(product);
            productList.add(productCard, col, row);
            
            col++;
            if (col >= maxCols) {
                col = 0;
                row++;
            }
        }
    }
    
    /**
     * Creates a product card VBox for display.
     */
    private VBox createProductCard(ProductDTO product) {
        VBox card = new VBox(15);
        card.getStyleClass().add("card");
        card.setPrefWidth(260);
        card.setMinWidth(260);
        card.setMaxWidth(260);
        
        // Image container with stacking for placeholder and actual image
        StackPane imageContainer = new StackPane();
        imageContainer.getStyleClass().add("image-placeholder");
        imageContainer.setPrefHeight(200);
        imageContainer.setPrefWidth(260);
        
        // Default emoji placeholder
        Label placeholderLabel = new Label("📦");
        placeholderLabel.setStyle("-fx-font-size: 64px;");
        
        ImageView imageView = new ImageView();
        imageView.setFitWidth(240);
        imageView.setFitHeight(180);
        imageView.setPreserveRatio(true);
        
        // Attempt to load actual image
        if (product.getImageUrl() != null && !product.getImageUrl().trim().isEmpty()) {
            try {
                Image img = new Image(product.getImageUrl(), true); // Background load
                img.errorProperty().addListener((obs, oldVal, newVal) -> {
                    if (newVal) placeholderLabel.setVisible(true);
                });
                img.progressProperty().addListener((obs, oldVal, newVal) -> {
                    if (newVal.doubleValue() == 1.0 && !img.isError()) {
                        placeholderLabel.setVisible(false);
                    }
                });
                imageView.setImage(img);
                // If already loaded or loading, hide placeholder initially if not error
                if (!img.isError()) placeholderLabel.setVisible(false);
            } catch (Exception e) {
                placeholderLabel.setVisible(true);
            }
        }
        
        imageContainer.getChildren().addAll(placeholderLabel, imageView);
        
        Label nameLabel = new Label(product.getName());
        nameLabel.getStyleClass().add("label-subtitle");
        nameLabel.setWrapText(true);
        nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        nameLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        nameLabel.setMinHeight(40);
        
        Label priceLabel = new Label("$" + String.format("%.2f", product.getPrice()));
        priceLabel.getStyleClass().add("label-success");
        priceLabel.getStyleClass().add("label-price");
        
        Button selectButton = new Button("View Details");
        selectButton.getStyleClass().add("button-primary");
        selectButton.setMaxWidth(Double.MAX_VALUE);
        selectButton.setOnAction(e -> {
            selectedProduct = product;
            handleViewDetails();
        });
        
        card.getChildren().addAll(imageContainer, nameLabel, priceLabel, selectButton);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPadding(new Insets(15));
        
        return card;
    }
    
    /**
     * Handles view details button click.
     */
    private void handleViewDetails() {
        if (selectedProduct == null) {
            // Try to get first product if none selected
            List<ProductDTO> products = productService.getAllProducts();
            if (products.isEmpty()) {
                return;
            }
            selectedProduct = products.get(0);
        }
        
        // Store selected product ID in session or pass to ProductDetailsController
        // For now, we'll need to pass product ID somehow
        // This is a simplified approach - in production, use a proper state management
        navigateToProductDetails(selectedProduct);
    }
    
    /**
     * Navigates to Product Details screen.
     */
    private void navigateToProductDetails(ProductDTO product) {
        // Store product in state manager
        ProductStateManager.setProductToView(product);
        
        Scene scene = productList.getScene();
        if (scene != null) {
            NavigationHelper.navigateTo("ProductDetails.fxml", NavigationHelper.getStage(scene));
        }
    }
    
    /**
     * Navigates to Login screen.
     */
    private void navigateToLogin() {
        Scene scene = searchField.getScene();
        if (scene != null) {
            NavigationHelper.navigateTo("Login.fxml", NavigationHelper.getStage(scene));
        }
    }
}

