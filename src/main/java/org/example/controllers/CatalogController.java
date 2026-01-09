package org.example.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
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
    private Button viewDetailsButton;
    
    @FXML
    private Label perfLabel;
    
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
        viewDetailsButton.setOnAction(e -> handleViewDetails());
        
        // Load products
        loadProducts();
    }
    
    /**
     * Loads all products and displays them.
     */
    private void loadProducts() {
        try {
            List<ProductDTO> products = productService.getAllProducts();
            
            if (products == null) {
                products = new java.util.ArrayList<>();
                perfLabel.setText("Error: Failed to load products");
                return;
            }
            
            // Performance timing is logged by ProductService via PerformanceMonitor
            // Display product count and note that timing is in console
            perfLabel.setText("Loaded " + products.size() + " products (check console for performance timing)");
            
            displayProducts(products);
        } catch (Exception e) {
            System.err.println("Error loading products: " + e.getMessage());
            e.printStackTrace();
            perfLabel.setText("Error loading products. Please try again.");
            displayProducts(new java.util.ArrayList<>());
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
            perfLabel.setText("Found " + products.size() + " products (check console for performance timing)");
            
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
            perfLabel.setText("Sorted " + products.size() + " products (check console for performance timing)");
            
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
        VBox card = new VBox(10);
        card.setStyle("-fx-padding: 10; -fx-border-color: #ccc; -fx-border-radius: 5;");
        
        Label nameLabel = new Label(product.getName());
        nameLabel.setStyle("-fx-font-weight: bold;");
        
        Label priceLabel = new Label("$" + product.getPrice());
        priceLabel.setStyle("-fx-text-fill: green; -fx-font-size: 14px;");
        
        Button selectButton = new Button("View Details");
        selectButton.setOnAction(e -> {
            selectedProduct = product;
            handleViewDetails();
        });
        
        card.getChildren().addAll(nameLabel, priceLabel, selectButton);
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
        
        Scene scene = viewDetailsButton.getScene();
        if (scene != null) {
            NavigationHelper.navigateTo("ProductDetails.fxml", NavigationHelper.getStage(scene));
        }
    }
    
    /**
     * Navigates to Login screen.
     */
    private void navigateToLogin() {
        Scene scene = viewDetailsButton.getScene();
        if (scene != null) {
            NavigationHelper.navigateTo("Login.fxml", NavigationHelper.getStage(scene));
        }
    }
}

