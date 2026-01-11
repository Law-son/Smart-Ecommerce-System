package org.example.utils;

import org.example.dto.ProductDTO;

/**
 * Simple state manager for passing product data between controllers.
 */
public class ProductStateManager {
    private static ProductDTO currentProduct;
    
    /**
     * Sets the product to view.
     *
     * @param product ProductDTO to view
     */
    public static void setProductToView(ProductDTO product) {
        currentProduct = product;
    }
    
    /**
     * Gets and clears the product to view.
     *
     * @return ProductDTO to view, or null if none set
     */
    public static ProductDTO getAndClearProductToView() {
        ProductDTO product = currentProduct;
        currentProduct = null;
        return product;
    }
}




