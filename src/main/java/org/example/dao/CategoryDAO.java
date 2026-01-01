package org.example.dao;

import org.example.configs.DatabaseConfig;
import org.example.dto.CategoryDTO;
import org.example.models.Category;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Category operations.
 * Handles all database interactions for Category entities.
 */
public class CategoryDAO {

    /**
     * Retrieves a category by ID.
     *
     * @param id The category ID
     * @return Category object if found, null otherwise
     */
    public Category getCategoryById(int id) {
        String sql = "SELECT category_id, category_name, created_at FROM categories WHERE category_id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Category category = new Category();
                    category.setCategoryId(rs.getInt("category_id"));
                    category.setCategoryName(rs.getString("category_name"));
                    Timestamp createdAt = rs.getTimestamp("created_at");
                    if (createdAt != null) {
                        category.setCreatedAt(createdAt.toLocalDateTime());
                    }
                    return category;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving category by ID: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }

    /**
     * Retrieves all categories from the database.
     *
     * @return List of all Category objects
     */
    public List<Category> getAllCategories() {
        String sql = "SELECT category_id, category_name, created_at FROM categories ORDER BY category_name";
        List<Category> categories = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                Category category = new Category();
                category.setCategoryId(rs.getInt("category_id"));
                category.setCategoryName(rs.getString("category_name"));
                Timestamp createdAt = rs.getTimestamp("created_at");
                if (createdAt != null) {
                    category.setCreatedAt(createdAt.toLocalDateTime());
                }
                categories.add(category);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving all categories: " + e.getMessage());
            e.printStackTrace();
        }
        
        return categories;
    }

    /**
     * Creates a new category.
     *
     * @param dto The category data transfer object
     * @return true if category was created successfully, false otherwise
     */
    public boolean createCategory(CategoryDTO dto) {
        String sql = "INSERT INTO categories (category_name) VALUES (?)";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, dto.getCategoryName());
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error creating category: " + e.getMessage());
            if (e.getSQLState().equals("23505")) { // Unique constraint violation
                System.err.println("Category name already exists: " + dto.getCategoryName());
            }
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Deletes a category by ID.
     *
     * @param id The category ID to delete
     * @return true if category was deleted successfully, false otherwise
     */
    public boolean deleteCategory(int id) {
        String sql = "DELETE FROM categories WHERE category_id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error deleting category: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Updates an existing category.
     *
     * @param id The category ID to update
     * @param dto The updated category data
     * @return true if category was updated successfully, false otherwise
     */
    public boolean updateCategory(int id, CategoryDTO dto) {
        String sql = "UPDATE categories SET category_name = ? WHERE category_id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, dto.getCategoryName());
            pstmt.setInt(2, id);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating category: " + e.getMessage());
            if (e.getSQLState().equals("23505")) { // Unique constraint violation
                System.err.println("Category name already exists: " + dto.getCategoryName());
            }
            e.printStackTrace();
            return false;
        }
    }
}

