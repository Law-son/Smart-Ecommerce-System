package org.example.services;

import org.example.dao.ProductDAO;
import org.example.dto.OrderItemDTO;
import org.example.models.Product;
import org.example.utils.CartCalculator;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Cart service handling in-memory shopping cart operations.
 * Manages cart items, totals, and stock validation before checkout.
 * Follows Single Responsibility Principle by delegating calculations to CartCalculator.
 */
public class CartService {
    private final InventoryService inventoryService;
    private final ProductDAO productDAO;
    private final CartCalculator calculator;
    
    // In-memory cart: productId -> OrderItemDTO
    private final Map<Integer, OrderItemDTO> cartItems;
    
    public CartService() {
        this.inventoryService = new InventoryService();
        this.productDAO = new ProductDAO();
        this.calculator = new CartCalculator();
        this.cartItems = new HashMap<>();
    }
    
    /**
     * Adds a product to the cart with stock validation.
     *
     * @param productId Product ID
     * @param quantity Quantity to add
     * @return true if item was added successfully, false otherwise
     */
    public boolean addToCart(int productId, int quantity) {
        if (quantity <= 0) {
            System.err.println("Quantity must be greater than zero");
            return false;
        }
        
        // Validate stock availability
        if (!inventoryService.checkStock(productId, quantity)) {
            int availableStock = inventoryService.getAvailableStock(productId);
            System.err.println("Insufficient stock for product " + productId + 
                             ". Requested " + quantity + ", Available " + availableStock);
            return false;
        }
        
        // Get product to retrieve price
        Product product = productDAO.getProductById(productId);
        if (product == null) {
            System.err.println("Product not found: " + productId);
            return false;
        }
        
        // Check if item already exists in cart
        if (cartItems.containsKey(productId)) {
            OrderItemDTO existingItem = cartItems.get(productId);
            int newQuantity = existingItem.getQuantity() + quantity;
            
            // Validate stock for new total quantity
            if (!inventoryService.checkStock(productId, newQuantity)) {
                int availableStock = inventoryService.getAvailableStock(productId);
                System.err.println("Insufficient stock for product " + productId + 
                                 ". Requested " + newQuantity + ", Available " + availableStock);
                return false;
            }
            
            existingItem.setQuantity(newQuantity);
        } else {
            // Create new cart item
            OrderItemDTO newItem = new OrderItemDTO();
            newItem.setProductId(productId);
            newItem.setQuantity(quantity);
            newItem.setUnitPrice(product.getPrice());
            cartItems.put(productId, newItem);
        }
        
        System.out.println("Added " + quantity + " of product " + productId + " to cart");
        return true;
    }
    
    /**
     * Removes a product from the cart.
     *
     * @param productId Product ID
     * @return true if item was removed successfully, false otherwise
     */
    public boolean removeFromCart(int productId) {
        if (cartItems.remove(productId) != null) {
            System.out.println("Removed product " + productId + " from cart");
            return true;
        }
        System.err.println("Product " + productId + " not found in cart");
        return false;
    }
    
    /**
     * Updates the quantity of a cart item with stock validation.
     *
     * @param productId Product ID
     * @param quantity New quantity
     * @return true if update was successful, false otherwise
     */
    public boolean updateCartItem(int productId, int quantity) {
        if (quantity <= 0) {
            // If quantity is 0 or negative, remove from cart
            return removeFromCart(productId);
        }
        
        if (!cartItems.containsKey(productId)) {
            System.err.println("Product " + productId + " not found in cart");
            return false;
        }
        
        // Validate stock availability
        if (!inventoryService.checkStock(productId, quantity)) {
            int availableStock = inventoryService.getAvailableStock(productId);
            System.err.println("Insufficient stock for product " + productId + 
                             ". Requested " + quantity + ", Available " + availableStock);
            return false;
        }
        
        OrderItemDTO item = cartItems.get(productId);
        item.setQuantity(quantity);
        
        System.out.println("Updated quantity for product " + productId + " to " + quantity);
        return true;
    }
    
    /**
     * Gets all cart items.
     *
     * @return List of OrderItemDTO objects in the cart
     */
    public List<OrderItemDTO> getCartItems() {
        return new ArrayList<>(cartItems.values());
    }
    
    /**
     * Clears all items from the cart.
     */
    public void clearCart() {
        cartItems.clear();
        System.out.println("Cart cleared");
    }
    
    /**
     * Calculates the total price of all items in the cart.
     * Uses BigDecimal for precise decimal calculations.
     *
     * @return Total cart amount as BigDecimal
     */
    public BigDecimal calculateCartTotal() {
        return calculator.calculateTotal(new ArrayList<>(cartItems.values()));
    }
    
    /**
     * Gets the number of items in the cart.
     *
     * @return Number of distinct products in cart
     */
    public int getCartItemCount() {
        return cartItems.size();
    }
    
    /**
     * Gets the total quantity of all items in the cart.
     *
     * @return Total quantity of all items
     */
    public int getTotalQuantity() {
        return calculator.getTotalQuantity(new ArrayList<>(cartItems.values()));
    }
}

