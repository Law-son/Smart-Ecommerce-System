package org.example.controllers;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.Scene;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import org.example.dao.InventoryDAO;
import org.example.dao.ProductDAO;
import org.example.dao.UserDAO;
import org.example.dto.CategoryDTO;
import org.example.dto.ProductDTO;
import org.example.models.Category;
import org.example.models.Inventory;
import org.example.models.Order;
import org.example.models.Product;
import org.example.models.User;
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
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    private TableColumn<ProductDTO, Integer> productIndexCol;
    @FXML
    private TableColumn<ProductDTO, String> productNameCol;
    @FXML
    private TableColumn<ProductDTO, BigDecimal> productPriceCol;
    @FXML
    private TableColumn<ProductDTO, String> productCategoryCol;
    @FXML
    private TableColumn<ProductDTO, Integer> productStockCol;
    
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
    private TableColumn<Category, Integer> categoryIndexCol;
    @FXML
    private TableColumn<Category, String> categoryNameCol;
    
    // Orders tab
    @FXML
    private TableView<Order> adminOrderTable;
    @FXML
    private TableColumn<Order, Integer> orderIndexCol;
    @FXML
    private TableColumn<Order, Integer> orderIdCol;
    @FXML
    private TableColumn<Order, String> orderCustomerCol;
    @FXML
    private TableColumn<Order, java.time.LocalDateTime> orderDateCol;
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
    private TableColumn<Inventory, Integer> inventoryIndexCol;
    @FXML
    private TableColumn<Inventory, String> inventoryProductNameCol;
    @FXML
    private TableColumn<Inventory, Integer> stockQuantityCol;
    @FXML
    private Button updateStockBtn;
    
    // Performance label
    @FXML
    private Label adminPerfLabel;
    
    @FXML
    private Button logoutButton;
    
    @FXML
    private Button refreshDataBtn;
    
    @FXML
    private ProgressIndicator productsLoadingIndicator;
    
    @FXML
    private ProgressIndicator categoriesLoadingIndicator;
    
    @FXML
    private ProgressIndicator ordersLoadingIndicator;
    
    @FXML
    private ProgressIndicator inventoryLoadingIndicator;
    
    private ProductService productService;
    private CategoryService categoryService;
    private OrderService orderService;
    private InventoryService inventoryService;
    private InventoryDAO inventoryDAO;
    private ProductDAO productDAO;
    private UserDAO userDAO;
    private SessionManager sessionManager;
    private ExecutorService executorService = Executors.newFixedThreadPool(4); // For async tasks
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.productService = new ProductService();
        this.categoryService = new CategoryService();
        this.orderService = new OrderService();
        this.inventoryService = new InventoryService();
        this.inventoryDAO = new InventoryDAO();
        this.productDAO = new ProductDAO();
        this.userDAO = new UserDAO();
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
        productIndexCol.setCellFactory(col -> new TableCell<ProductDTO, Integer>() {
            @Override
            public void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(String.valueOf(getIndex() + 1));
                }
            }
        });
        
        productNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        productPriceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        
        productCategoryCol.setCellValueFactory(column -> {
            int catId = column.getValue().getCategoryId();
            List<Category> categories = categoryService.getAllCategories();
            for (Category cat : categories) {
                if (cat.getCategoryId() == catId) {
                    return new ReadOnlyObjectWrapper<>(cat.getCategoryName());
                }
            }
            return new ReadOnlyObjectWrapper<>("Unknown");
        });
        
        productStockCol.setCellValueFactory(column -> {
            try {
                // Find product ID by name since DTO might not have it
                List<Product> allProducts = productDAO.getAllProducts();
                for (Product p : allProducts) {
                    if (p.getName().equals(column.getValue().getName())) {
                        Inventory inv = inventoryDAO.getInventoryByProduct(p.getProductId());
                        return new ReadOnlyObjectWrapper<>(inv != null ? inv.getQuantity() : 0);
                    }
                }
            } catch (Exception e) {
                System.err.println("Error fetching stock for table: " + e.getMessage());
            }
            return new ReadOnlyObjectWrapper<>(0);
        });
    }
    
    /**
     * Initializes category table columns.
     */
    private void initializeCategoryTable() {
        categoryIndexCol.setCellFactory(col -> new TableCell<Category, Integer>() {
            @Override
            public void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(String.valueOf(getIndex() + 1));
                }
            }
        });
        categoryNameCol.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
    }
    
    /**
     * Initializes order table columns.
     */
    private void initializeOrderTable() {
        orderIndexCol.setCellFactory(col -> new TableCell<Order, Integer>() {
            @Override
            public void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(String.valueOf(getIndex() + 1));
                }
            }
        });
        orderIdCol.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        orderCustomerCol.setCellValueFactory(column -> {
            User user = userDAO.getUserById(column.getValue().getUserId());
            return new ReadOnlyObjectWrapper<>(user != null ? user.getEmail() : "Unknown");
        });
        orderDateCol.setCellValueFactory(new PropertyValueFactory<>("orderDate"));
        orderTotalCol.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        orderStatusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
    }
    
    /**
     * Initializes inventory table columns.
     */
    private void initializeInventoryTable() {
        inventoryIndexCol.setCellFactory(col -> new TableCell<Inventory, Integer>() {
            @Override
            public void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(String.valueOf(getIndex() + 1));
                }
            }
        });
        inventoryProductNameCol.setCellValueFactory(column -> {
            Product product = productDAO.getProductById(column.getValue().getProductId());
            return new ReadOnlyObjectWrapper<>(product != null ? product.getName() : "Unknown");
        });
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
        
        logoutButton.setOnAction(e -> handleLogout());
        refreshDataBtn.setOnAction(e -> loadAllData());
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
     * Loads all data for admin panel.
     */
    private void loadAllData() {
        loadProducts();
        loadCategories();
        loadOrders();
        loadInventory();
        
        adminPerfLabel.setText("Admin panel data refreshed");
    }
    
    /**
     * Loads products into table.
     */
    private void loadProducts() {
        try {
            showProductsLoading(true);
            List<ProductDTO> products = productService.getAllProducts();
            adminProductTable.getItems().clear();
            adminProductTable.getItems().addAll(products);
            showProductsLoading(false);
        } catch (Exception e) {
            System.err.println("Error loading products: " + e.getMessage());
            e.printStackTrace();
            showProductsLoading(false);
        }
    }
    
    /**
     * Shows or hides the products loading indicator.
     */
    private void showProductsLoading(boolean show) {
        if (productsLoadingIndicator != null) {
            productsLoadingIndicator.setVisible(show);
            productsLoadingIndicator.setManaged(show);
        }
    }
    
    /**
     * Loads categories into table.
     */
    private void loadCategories() {
        try {
            showCategoriesLoading(true);
            List<Category> categories = categoryService.getAllCategories();
            adminCategoryTable.getItems().clear();
            adminCategoryTable.getItems().addAll(categories);
            showCategoriesLoading(false);
        } catch (Exception e) {
            System.err.println("Error loading categories: " + e.getMessage());
            e.printStackTrace();
            showCategoriesLoading(false);
        }
    }
    
    /**
     * Shows or hides the categories loading indicator.
     */
    private void showCategoriesLoading(boolean show) {
        if (categoriesLoadingIndicator != null) {
            categoriesLoadingIndicator.setVisible(show);
            categoriesLoadingIndicator.setManaged(show);
        }
    }
    
    /**
     * Loads orders into table.
     */
    private void loadOrders() {
        try {
            showOrdersLoading(true);
            List<Order> orders = orderService.getAllOrders();
            adminOrderTable.getItems().clear();
            adminOrderTable.getItems().addAll(orders);
            showOrdersLoading(false);
        } catch (Exception e) {
            System.err.println("Error loading orders: " + e.getMessage());
            e.printStackTrace();
            showOrdersLoading(false);
        }
    }
    
    /**
     * Shows or hides the orders loading indicator.
     */
    private void showOrdersLoading(boolean show) {
        if (ordersLoadingIndicator != null) {
            ordersLoadingIndicator.setVisible(show);
            ordersLoadingIndicator.setManaged(show);
        }
    }
    
    /**
     * Loads inventory into table.
     */
    private void loadInventory() {
        try {
            showInventoryLoading(true);
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
            showInventoryLoading(false);
        } catch (Exception e) {
            System.err.println("Error loading inventory: " + e.getMessage());
            e.printStackTrace();
            showInventoryLoading(false);
        }
    }
    
    /**
     * Shows or hides the inventory loading indicator.
     */
    private void showInventoryLoading(boolean show) {
        if (inventoryLoadingIndicator != null) {
            inventoryLoadingIndicator.setVisible(show);
            inventoryLoadingIndicator.setManaged(show);
        }
    }
    
    /**
     * Handles add product button click.
     */
    private void handleAddProduct() {
        // Create dialog for product input
        Dialog<ProductDTO> dialog = createProductDialog(null);
        dialog.setTitle("Add Product");
        dialog.setHeaderText("Create New Product");
        
        dialog.showAndWait().ifPresent(productDTO -> {
            if (productDTO != null) {
                boolean success = productService.createProduct(productDTO);
                if (success) {
                    loadAllData();
                    adminPerfLabel.setText("Product created successfully");
                } else {
                    adminPerfLabel.setText("Failed to create product. Check inputs and try again.");
                }
            }
        });
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
        
        // Get product ID from name using ProductDAO
        int productId = getProductIdByName(selected.getName());
        if (productId <= 0) {
            adminPerfLabel.setText("Could not find product ID. Please try again.");
            return;
        }
        
        // Create dialog with existing product data
        Dialog<ProductDTO> dialog = createProductDialog(selected);
        dialog.setTitle("Edit Product");
        dialog.setHeaderText("Edit Product: " + selected.getName());
        
        dialog.showAndWait().ifPresent(productDTO -> {
            if (productDTO != null) {
                boolean success = productService.updateProduct(productId, productDTO);
                if (success) {
                    loadAllData();
                    adminPerfLabel.setText("Product updated successfully");
                } else {
                    adminPerfLabel.setText("Failed to update product. Check inputs and try again.");
                }
            }
        });
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
        
        // Get product ID from name
        int productId = getProductIdByName(selected.getName());
        if (productId <= 0) {
            adminPerfLabel.setText("Could not find product ID. Please try again.");
            return;
        }
        
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Delete Product");
        confirmAlert.setHeaderText("Confirm Deletion");
        confirmAlert.setContentText("Are you sure you want to delete product: " + selected.getName() + "?");
        
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                boolean success = productService.deleteProduct(productId);
                if (success) {
                    loadAllData();
                    adminPerfLabel.setText("Product deleted successfully");
                } else {
                    adminPerfLabel.setText("Failed to delete product");
                }
            }
        });
    }
    
    /**
     * Gets product ID by name from database.
     *
     * @param productName Product name
     * @return Product ID, or -1 if not found
     */
    private int getProductIdByName(String productName) {
        if (productName == null || productName.isEmpty()) {
            return -1;
        }
        
        List<Product> products = productDAO.getAllProducts();
        for (Product product : products) {
            if (productName.equals(product.getName())) {
                return product.getProductId();
            }
        }
        return -1;
    }
    
    /**
     * Creates a dialog for product input.
     *
     * @param existingProduct Existing product data (null for new product)
     * @return Dialog with ProductDTO result
     */
    private Dialog<ProductDTO> createProductDialog(ProductDTO existingProduct) {
        Dialog<ProductDTO> dialog = new Dialog<>();
        dialog.setResizable(true);
        dialog.setTitle(existingProduct == null ? "Add New Product" : "Edit Product");
        
        // Create form fields
        TextField nameField = new TextField();
        TextArea descriptionField = new TextArea();
        TextField priceField = new TextField();
        ComboBox<Category> categoryComboBox = new ComboBox<>();
        TextField imageUrlField = new TextField();
        
        // Load categories into ComboBox
        try {
            List<Category> categories = categoryService.getAllCategories();
            categoryComboBox.getItems().addAll(categories);
            
            // Set cell factory to show category name
            categoryComboBox.setConverter(new javafx.util.StringConverter<Category>() {
                @Override
                public String toString(Category category) {
                    return category == null ? "" : category.getCategoryName();
                }

                @Override
                public Category fromString(String string) {
                    return null; // Not needed
                }
            });
            
            // Select existing category if editing
            if (existingProduct != null) {
                Category selected = categories.stream()
                    .filter(c -> c.getCategoryId() == existingProduct.getCategoryId())
                    .findFirst()
                    .orElse(null);
                categoryComboBox.setValue(selected);
            } else if (!categories.isEmpty()) {
                categoryComboBox.setValue(categories.get(0));
            }
        } catch (Exception e) {
            System.err.println("Error loading categories for dialog: " + e.getMessage());
        }
        
        // Set existing values if editing
        if (existingProduct != null) {
            nameField.setText(existingProduct.getName());
            descriptionField.setText(existingProduct.getDescription());
            priceField.setText(existingProduct.getPrice() != null ? existingProduct.getPrice().toString() : "");
            imageUrlField.setText(existingProduct.getImageUrl() != null ? existingProduct.getImageUrl() : "");
        }
        
        // Set field properties
        descriptionField.setPrefRowCount(3);
        descriptionField.setWrapText(true);
        categoryComboBox.setMaxWidth(Double.MAX_VALUE);
        
        // Create form layout
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new javafx.geometry.Insets(24));
        
        grid.add(new Label("Product Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descriptionField, 1, 1);
        grid.add(new Label("Price:"), 0, 2);
        grid.add(priceField, 1, 2);
        grid.add(new Label("Category:"), 0, 3);
        grid.add(categoryComboBox, 1, 3);
        grid.add(new Label("Image URL:"), 0, 4);
        grid.add(imageUrlField, 1, 4);
        
        // Set column constraints for responsiveness
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setMinWidth(100);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(col1, col2);
        
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getStyleClass().add("container");
        
        // Add buttons
        ButtonType saveButtonType = new ButtonType("Save Product", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        // Style buttons
        Button saveBtn = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
        saveBtn.getStyleClass().add("button-primary");
        Button cancelBtn = (Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL);
        cancelBtn.getStyleClass().add("button-secondary");
        
        // Validate and convert result
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    if (nameField.getText().trim().isEmpty()) throw new Exception("Name is required");
                    if (priceField.getText().trim().isEmpty()) throw new Exception("Price is required");
                    if (categoryComboBox.getValue() == null) throw new Exception("Category is required");
                    
                    ProductDTO productDTO = new ProductDTO();
                    productDTO.setName(nameField.getText().trim());
                    productDTO.setDescription(descriptionField.getText().trim());
                    productDTO.setPrice(new BigDecimal(priceField.getText().trim()));
                    productDTO.setCategoryId(categoryComboBox.getValue().getCategoryId());
                    productDTO.setImageUrl(imageUrlField.getText().trim());
                    return productDTO;
                } catch (NumberFormatException e) {
                    adminPerfLabel.setText("Error: Price must be a valid number.");
                    return null;
                } catch (Exception e) {
                    adminPerfLabel.setText("Error: " + e.getMessage());
                    return null;
                }
            }
            return null;
        });
        
        return dialog;
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
                loadAllData();
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
                loadAllData();
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
                    loadAllData();
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
        
        User user = userDAO.getUserById(selected.getUserId());
        String customerInfo = user != null ? user.getEmail() : "User #" + selected.getUserId();
        
        ChoiceDialog<String> dialog = new ChoiceDialog<>(selected.getStatus());
        dialog.getItems().addAll("PENDING", "PAID", "SHIPPED", "DELIVERED", "RECEIVED", "CANCELLED");
        dialog.setTitle("Update Order Status");
        dialog.setHeaderText("Update Order #" + selected.getOrderId() + " (Customer: " + customerInfo + ")");
        dialog.setContentText("Select new status:");
        
        dialog.showAndWait().ifPresent(newStatus -> {
            boolean success = orderService.updateOrderStatus(selected.getOrderId(), newStatus);
            if (success) {
                loadAllData();
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
        
        Product product = productDAO.getProductById(selected.getProductId());
        String productName = product != null ? product.getName() : "#" + selected.getProductId();
        
        TextInputDialog dialog = new TextInputDialog(String.valueOf(selected.getQuantity()));
        dialog.setTitle("Update Stock");
        dialog.setHeaderText("Update Stock for: " + productName);
        dialog.setContentText("Enter new stock quantity:");
        
        dialog.showAndWait().ifPresent(quantityStr -> {
            try {
                int newQuantity = Integer.parseInt(quantityStr);
                boolean success = inventoryService.updateStock(selected.getProductId(), newQuantity);
                if (success) {
                    loadAllData();
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
     * Navigates to Login screen.
     */
    private void navigateToLogin() {
        Scene scene = logoutButton.getScene();
        if (scene != null) {
            NavigationHelper.navigateTo("Login.fxml", NavigationHelper.getStage(scene));
        }
    }
}

