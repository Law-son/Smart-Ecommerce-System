package org.example.dao;

import org.example.configs.DatabaseConfig;
import org.example.dto.ProductDTO;
import org.example.models.Product;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Product operations.
 * Handles all database interactions for Product entities.
 */
public class ProductDAO {

    /**
     * Retrieves a product by ID.
     *
     * @param id The product ID
     * @return Product object if found, null otherwise
     */
    public Product getProductById(int id) {
        String sql = "SELECT product_id, category_id, name, description, price, image_url, created_at FROM products WHERE product_id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Product product = new Product();
                    product.setProductId(rs.getInt("product_id"));
                    product.setCategoryId(rs.getInt("category_id"));
                    product.setName(rs.getString("name"));
                    product.setDescription(rs.getString("description"));
                    product.setPrice(rs.getBigDecimal("price"));
                    product.setImageUrl(rs.getString("image_url"));
                    Timestamp createdAt = rs.getTimestamp("created_at");
                    if (createdAt != null) {
                        product.setCreatedAt(createdAt.toLocalDateTime());
                    }
                    return product;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving product by ID: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }

    /**
     * Retrieves all products from the database.
     *
     * @return List of all Product objects
     */
    public List<Product> getAllProducts() {
        String sql = "SELECT product_id, category_id, name, description, price, image_url, created_at FROM products ORDER BY name";
        List<Product> products = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                Product product = new Product();
                product.setProductId(rs.getInt("product_id"));
                product.setCategoryId(rs.getInt("category_id"));
                product.setName(rs.getString("name"));
                product.setDescription(rs.getString("description"));
                product.setPrice(rs.getBigDecimal("price"));
                product.setImageUrl(rs.getString("image_url"));
                Timestamp createdAt = rs.getTimestamp("created_at");
                if (createdAt != null) {
                    product.setCreatedAt(createdAt.toLocalDateTime());
                }
                products.add(product);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving all products: " + e.getMessage());
            e.printStackTrace();
        }
        
        return products;
    }

    /**
     * Creates a new product.
     *
     * @param dto The product data transfer object
     * @return true if product was created successfully, false otherwise
     */
    public boolean createProduct(ProductDTO dto) {
        String sql = "INSERT INTO products (category_id, name, description, price, image_url) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, dto.getCategoryId());
            pstmt.setString(2, dto.getName());
            pstmt.setString(3, dto.getDescription());
            pstmt.setBigDecimal(4, dto.getPrice());
            pstmt.setString(5, dto.getImageUrl());
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error creating product: " + e.getMessage());
            if (e.getSQLState().equals("23503")) { // Foreign key constraint violation
                System.err.println("Category ID does not exist: " + dto.getCategoryId());
            }
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Deletes a product by ID.
     *
     * @param id The product ID to delete
     * @return true if product was deleted successfully, false otherwise
     */
    public boolean deleteProduct(int id) {
        String sql = "DELETE FROM products WHERE product_id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error deleting product: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Updates an existing product.
     *
     * @param id The product ID to update
     * @param dto The updated product data
     * @return true if product was updated successfully, false otherwise
     */
    public boolean updateProduct(int id, ProductDTO dto) {
        String sql = "UPDATE products SET category_id = ?, name = ?, description = ?, price = ?, image_url = ? WHERE product_id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, dto.getCategoryId());
            pstmt.setString(2, dto.getName());
            pstmt.setString(3, dto.getDescription());
            pstmt.setBigDecimal(4, dto.getPrice());
            pstmt.setString(5, dto.getImageUrl());
            pstmt.setInt(6, id);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating product: " + e.getMessage());
            if (e.getSQLState().equals("23503")) { // Foreign key constraint violation
                System.err.println("Category ID does not exist: " + dto.getCategoryId());
            }
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Searches for products by name (case-insensitive).
     *
     * @param keyword The search keyword
     * @return List of Product objects matching the keyword
     */
    public List<Product> searchProductsByName(String keyword) {
        String sql = "SELECT product_id, category_id, name, description, price, image_url, created_at FROM products WHERE LOWER(name) LIKE LOWER(?) ORDER BY name";
        List<Product> products = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, "%" + keyword + "%");
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Product product = new Product();
                    product.setProductId(rs.getInt("product_id"));
                    product.setCategoryId(rs.getInt("category_id"));
                    product.setName(rs.getString("name"));
                    product.setDescription(rs.getString("description"));
                    product.setPrice(rs.getBigDecimal("price"));
                    product.setImageUrl(rs.getString("image_url"));
                    Timestamp createdAt = rs.getTimestamp("created_at");
                    if (createdAt != null) {
                        product.setCreatedAt(createdAt.toLocalDateTime());
                    }
                    products.add(product);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error searching products by name: " + e.getMessage());
            e.printStackTrace();
        }
        
        return products;
    }
}

