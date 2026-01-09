package org.example.utils.mappers;

import org.example.dto.ProductDTO;
import org.example.models.Product;

import java.util.ArrayList;
import java.util.List;

/**
 * Mapper for converting between Product models and ProductDTOs.
 * Follows Single Responsibility Principle - only handles Product conversions.
 */
public class ProductMapper {
    
    /**
     * Converts a Product model to ProductDTO.
     *
     * @param product Product model
     * @return ProductDTO object
     */
    public ProductDTO toDTO(Product product) {
        if (product == null) {
            return null;
        }
        
        ProductDTO dto = new ProductDTO();
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setCategoryId(product.getCategoryId());
        dto.setImageUrl(product.getImageUrl());
        return dto;
    }
    
    /**
     * Converts a list of Product models to ProductDTO list.
     *
     * @param products List of Product models
     * @return List of ProductDTO objects
     */
    public List<ProductDTO> toDTOList(List<Product> products) {
        if (products == null) {
            return new ArrayList<>();
        }
        
        List<ProductDTO> dtos = new ArrayList<>();
        for (Product product : products) {
            ProductDTO dto = toDTO(product);
            if (dto != null) {
                dtos.add(dto);
            }
        }
        return dtos;
    }
}



