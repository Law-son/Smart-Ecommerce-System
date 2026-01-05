package org.example.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.Scene;
import org.example.dao.InventoryDAO;
import org.example.dao.ProductDAO;
import org.example.dto.CategoryDTO;
import org.example.dto.ProductDTO;
import org.example.models.Category;
import org.example.models.Inventory;
import org.example.models.Order;
import org.example.services.CategoryService;
import org.example.services.InventoryService;
import org.example.services.OrderService;
import org.example.services.ProductService;
import org.example.utils.NavigationHelper;
import org.example.utils.SessionManager;

import java.math.BigDecimal;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller for the Admin panel screen.
 * Handles product, category, order, and inventory management.
 */
public class AdminController implements Initializable {
    
    // Products tab
    @FXML
    private Button addProductBtn;
    @FXML
    private Button editProductBtn;
    @FXML
    private Button deleteProductBtn;
    @FXML
    private TableView<ProductDTO> adminProductTable;
    @FXML
    private TableColumn<ProductDTO, String> productNameCol;
    @FXML
    private TableColumn<ProductDTO, BigDecimal> productPriceCol;
    
    // Categories tab
    @FXML
    private Button addCategoryBtn;
    @FXML
    private Button editCategoryBtn;
    @FXML
    private Button deleteCategoryBtn;
    @FXML
    private TableView<Category> adminCategoryTable;
    @FXML
    private TableColumn<Category, String> categoryNameCol;
    
    // Orders tab
    @FXML
    private TableView<Order> adminOrderTable;
    @FXML
    private TableColumn<Order, Integer> orderIdCol;
    @FXML
    private TableColumn<Order, String> orderStatusCol;
    @FXML
    private TableColumn<Order, BigDecimal> orderTotalCol;
    @FXML
    private Button updateOrderStatusBtn;
    
    // Inventory tab
    @FXML
    private TableView<Inventory> inventoryTable;
    @FXML
    private TableColumn<Inventory, Integer> productIdCol;
    @FXML
    private TableColumn<Inventory, Integer> stockQuantityCol;
    @FXML
    private Button updateStockBtn;
    
    // Performance label
    @FXML
    private Label adminPerfLabel;
    
    // Test DB button
    @FXML
    private Button testDBButton;
    
    private ProductService productService;
    private CategoryService categoryService;
    private OrderService orderService;
    private InventoryService inventoryService;
    private InventoryDAO inventoryDAO;
    private ProductDAO productDAO;
    private SessionManager sessionManager;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.productService = new ProductService();
        this.categoryService = new CategoryService();
        this.orderService = new OrderService();
        this.inventoryService = new InventoryService();
        this.inventoryDAO = new InventoryDAO();
        this.productDAO = new ProductDAO();
        this.sessionManager = SessionManager.getInstance();
        
        // Check if user is logged in and is admin
        if (!sessionManager.isLoggedIn() || !sessionManager.isAdmin()) {
            System.err.println("Unauthorized access to Admin panel");
            navigateToLogin();
            return;
        }
        
        // Initialize table columns
        initializeProductTable();
        initializeCategoryTable();
        initializeOrderTable();
        initializeInventoryTable();
        
        // Set up event handlers
        setupEventHandlers();
        
        // Load data
        loadAllData();
    }
    
    /**
     * Initializes product table columns.
     */
    private void initializeProductTable() {
        productNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        productPriceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
    }
    
    /**
     * Initializes category table columns.
     */
    private void initializeCategoryTable() {
        categoryNameCol.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
    }
    
    /**
     * Initializes order table columns.
     */
    private void initializeOrderTable() {
        orderIdCol.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        orderStatusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        orderTotalCol.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
    }
    
    /**
     * Initializes inventory table columns.
     */
    private void initializeInventoryTable() {
        productIdCol.setCellValueFactory(new PropertyValueFactory<>("productId"));
        stockQuantityCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
    }
    
    /**
     * Sets up event handlers for buttons.
     */
    private void setupEventHandlers() {
        addProductBtn.setOnAction(e -> handleAddProduct());
        editProductBtn.setOnAction(e -> handleEditProduct());
        deleteProductBtn.setOnAction(e -> handleDeleteProduct());
        
        addCategoryBtn.setOnAction(e -> handleAddCategory());
        editCategoryBtn.setOnAction(e -> handleEditCategory());
        deleteCategoryBtn.setOnAction(e -> handleDeleteCategory());
        
        updateOrderStatusBtn.setOnAction(e -> handleUpdateOrderStatus());
        updateStockBtn.setOnAction(e -> handleUpdateStock());
        
        testDBButton.setOnAction(e -> handleTestDB());
    }
    
    /**
     * Loads all data for admin panel.
     */
    private void loadAllData() {
        long startTime = System.currentTimeMillis();
        
        loadProducts();
        loadCategories();
        loadOrders();
        loadInventory();
        
        long endTime = System.currentTimeMillis();
        adminPerfLabel.setText("Admin panel loaded in " + (endTime - startTime) + " ms");
    }
    
    /**
     * Loads products into table.
     */
    private void loadProducts() {
        List<ProductDTO> products = productService.getAllProducts();
        adminProductTable.getItems().clear();
        adminProductTable.getItems().addAll(products);
    }
    
    /**
     * Loads categories into table.
     */
    private void loadCategories() {
        List<Category> categories = categoryService.getAllCategories();
        adminCategoryTable.getItems().clear();
        adminCategoryTable.getItems().addAll(categories);
    }
    
    /**
     * Loads orders into table.
     */
    private void loadOrders() {
        List<Order> orders = orderService.getAllOrders();
        adminOrderTable.getItems().clear();
        adminOrderTable.getItems().addAll(orders);
    }
    
    /**
     * Loads inventory into table.
     */
    private void loadInventory() {
        // Get all products and their inventory
        List<Product> allProducts = productDAO.getAllProducts();
        inventoryTable.getItems().clear();
        
        for (Product product : allProducts) {
            Inventory inventory = inventoryDAO.getInventoryByProduct(product.getProductId());
            if (inventory != null) {
                inventoryTable.getItems().add(inventory);
            } else {
                // Create empty inventory entry if none exists
                Inventory emptyInventory = new Inventory();
                emptyInventory.setProductId(product.getProductId());
                emptyInventory.setQuantity(0);
                inventoryTable.getItems().add(emptyInventory);
            }
        }
    }
    
    /**
     * Handles add product button click.
     */
    private void handleAddProduct() {
        adminPerfLabel.setText("Add Product - Not yet implemented");
        // TODO: Open product creation dialog
    }
    
    /**
     * Handles edit product button click.
     */
    private void handleEditProduct() {
        ProductDTO selected = adminProductTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            adminPerfLabel.setText("Please select a product to edit");
            return;
        }
        adminPerfLabel.setText("Edit Product - Not yet implemented");
        // TODO: Open product edit dialog
    }
    
    /**
     * Handles delete product button click.
     */
    private void handleDeleteProduct() {
        ProductDTO selected = adminProductTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            adminPerfLabel.setText("Please select a product to delete");
            return;
        }
        adminPerfLabel.setText("Delete Product - Not yet implemented");
        // TODO: Implement product deletion via ProductService
    }
    
    /**
     * Handles add category button click.
     */
    private void handleAddCategory() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add Category");
        dialog.setHeaderText("Create New Category");
        dialog.setContentText("Category name:");
        
        dialog.showAndWait().ifPresent(categoryName -> {
            CategoryDTO categoryDTO = new CategoryDTO();
            categoryDTO.setCategoryName(categoryName);
            
            boolean success = categoryService.createCategory(categoryDTO);
            if (success) {
                loadCategories();
                adminPerfLabel.setText("Category created successfully");
            } else {
                adminPerfLabel.setText("Failed to create category");
            }
        });
    }
    
    /**
     * Handles edit category button click.
     */
    private void handleEditCategory() {
        Category selected = adminCategoryTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            adminPerfLabel.setText("Please select a category to edit");
            return;
        }
        
        TextInputDialog dialog = new TextInputDialog(selected.getCategoryName());
        dialog.setTitle("Edit Category");
        dialog.setHeaderText("Edit Category");
        dialog.setContentText("Category name:");
        
        dialog.showAndWait().ifPresent(categoryName -> {
            CategoryDTO categoryDTO = new CategoryDTO();
            categoryDTO.setCategoryName(categoryName);
            
            boolean success = categoryService.updateCategory(selected.getCategoryId(), categoryDTO);
            if (success) {
                loadCategories();
                adminPerfLabel.setText("Category updated successfully");
            } else {
                adminPerfLabel.setText("Failed to update category");
            }
        });
    }
    
    /**
     * Handles delete category button click.
     */
    private void handleDeleteCategory() {
        Category selected = adminCategoryTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            adminPerfLabel.setText("Please select a category to delete");
            return;
        }
        
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Delete Category");
        confirmAlert.setHeaderText("Confirm Deletion");
        confirmAlert.setContentText("Are you sure you want to delete category: " + selected.getCategoryName() + "?");
        
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                boolean success = categoryService.deleteCategory(selected.getCategoryId());
                if (success) {
                    loadCategories();
                    adminPerfLabel.setText("Category deleted successfully");
                } else {
                    adminPerfLabel.setText("Failed to delete category");
                }
            }
        });
    }
    
    /**
     * Handles update order status button click.
     */
    private void handleUpdateOrderStatus() {
        Order selected = adminOrderTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            adminPerfLabel.setText("Please select an order to update");
            return;
        }
        
        ChoiceDialog<String> dialog = new ChoiceDialog<>(selected.getStatus());
        dialog.getItems().addAll("PENDING", "PAID", "SHIPPED", "CANCELLED");
        dialog.setTitle("Update Order Status");
        dialog.setHeaderText("Update Order Status");
        dialog.setContentText("Select new status:");
        
        dialog.showAndWait().ifPresent(newStatus -> {
            boolean success = orderService.updateOrderStatus(selected.getOrderId(), newStatus);
            if (success) {
                loadOrders();
                adminPerfLabel.setText("Order status updated successfully");
            } else {
                adminPerfLabel.setText("Failed to update order status");
            }
        });
    }
    
    /**
     * Handles update stock button click.
     */
    private void handleUpdateStock() {
        Inventory selected = inventoryTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            adminPerfLabel.setText("Please select an inventory item to update");
            return;
        }
        
        TextInputDialog dialog = new TextInputDialog(String.valueOf(selected.getQuantity()));
        dialog.setTitle("Update Stock");
        dialog.setHeaderText("Update Stock for Product #" + selected.getProductId());
        dialog.setContentText("Enter new stock quantity:");
        
        dialog.showAndWait().ifPresent(quantityStr -> {
            try {
                int newQuantity = Integer.parseInt(quantityStr);
                boolean success = inventoryService.updateStock(selected.getProductId(), newQuantity);
                if (success) {
                    loadInventory();
                    adminPerfLabel.setText("Stock updated successfully");
                } else {
                    adminPerfLabel.setText("Failed to update stock");
                }
            } catch (NumberFormatException e) {
                adminPerfLabel.setText("Invalid quantity. Please enter a number.");
            }
        });
    }
    
    /**
     * Handles test DB button click.
     */
    private void handleTestDB() {
        org.example.configs.DatabaseConfig dbConfig = new org.example.configs.DatabaseConfig();
        boolean connected = org.example.configs.DatabaseConfig.testConnection();
        
        if (connected) {
            adminPerfLabel.setText("Database connection: SUCCESS");
        } else {
            adminPerfLabel.setText("Database connection: FAILED");
        }
    }
    
    /**
     * Navigates to Login screen.
     */
    private void navigateToLogin() {
        Scene scene = testDBButton.getScene();
        if (scene != null) {
            NavigationHelper.navigateTo("Login.fxml", NavigationHelper.getStage(scene));
        }
    }
}

