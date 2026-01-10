package org.example.utils;

import org.example.dto.ProductDTO;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for manual sorting algorithms.
 * Implements efficient sorting without using Collections.sort().
 */
public class SortingUtils {
    
    /**
     * Manual Quick Sort implementation for sorting products by price.
     *
     * @param products List of products to sort
     * @param ascending true for ascending, false for descending
     */
    public static void quickSortByPrice(List<ProductDTO> products, boolean ascending) {
        if (products == null || products.size() <= 1) {
            return;
        }
        quickSortByPriceHelper(products, 0, products.size() - 1, ascending);
    }
    
    private static void quickSortByPriceHelper(List<ProductDTO> products, int low, int high, boolean ascending) {
        if (low < high) {
            int pivotIndex = partitionByPrice(products, low, high, ascending);
            quickSortByPriceHelper(products, low, pivotIndex - 1, ascending);
            quickSortByPriceHelper(products, pivotIndex + 1, high, ascending);
        }
    }
    
    private static int partitionByPrice(List<ProductDTO> products, int low, int high, boolean ascending) {
        BigDecimal pivot = products.get(high).getPrice();
        int i = low - 1;
        
        for (int j = low; j < high; j++) {
            BigDecimal currentPrice = products.get(j).getPrice();
            boolean shouldSwap = ascending ? currentPrice.compareTo(pivot) <= 0 : currentPrice.compareTo(pivot) >= 0;
            
            if (shouldSwap) {
                i++;
                swap(products, i, j);
            }
        }
        
        swap(products, i + 1, high);
        return i + 1;
    }
    
    /**
     * Manual Merge Sort implementation for sorting products by name (A-Z).
     *
     * @param products List of products to sort
     */
    public static void mergeSortByName(List<ProductDTO> products) {
        if (products == null || products.size() <= 1) {
            return;
        }
        mergeSortByNameHelper(products, 0, products.size() - 1);
    }
    
    private static void mergeSortByNameHelper(List<ProductDTO> products, int left, int right) {
        if (left < right) {
            int mid = left + (right - left) / 2;
            mergeSortByNameHelper(products, left, mid);
            mergeSortByNameHelper(products, mid + 1, right);
            mergeByName(products, left, mid, right);
        }
    }
    
    private static void mergeByName(List<ProductDTO> products, int left, int mid, int right) {
        int n1 = mid - left + 1;
        int n2 = right - mid;
        
        List<ProductDTO> leftList = new ArrayList<>();
        List<ProductDTO> rightList = new ArrayList<>();
        
        for (int i = 0; i < n1; i++) {
            leftList.add(products.get(left + i));
        }
        for (int j = 0; j < n2; j++) {
            rightList.add(products.get(mid + 1 + j));
        }
        
        int i = 0, j = 0, k = left;
        
        while (i < n1 && j < n2) {
            String name1 = leftList.get(i).getName().toLowerCase();
            String name2 = rightList.get(j).getName().toLowerCase();
            
            if (name1.compareTo(name2) <= 0) {
                products.set(k, leftList.get(i));
                i++;
            } else {
                products.set(k, rightList.get(j));
                j++;
            }
            k++;
        }
        
        while (i < n1) {
            products.set(k, leftList.get(i));
            i++;
            k++;
        }
        
        while (j < n2) {
            products.set(k, rightList.get(j));
            j++;
            k++;
        }
    }
    
    /**
     * Manual Quick Sort implementation for sorting products by rating (descending).
     *
     * @param products List of products to sort
     * @param ratings Array of ratings corresponding to products (by product ID or index)
     */
    public static void quickSortByRating(List<ProductDTO> products, double[] ratings) {
        if (products == null || products.size() <= 1 || ratings == null || ratings.length != products.size()) {
            return;
        }
        quickSortByRatingHelper(products, ratings, 0, products.size() - 1);
    }
    
    private static void quickSortByRatingHelper(List<ProductDTO> products, double[] ratings, int low, int high) {
        if (low < high) {
            int pivotIndex = partitionByRating(products, ratings, low, high);
            quickSortByRatingHelper(products, ratings, low, pivotIndex - 1);
            quickSortByRatingHelper(products, ratings, pivotIndex + 1, high);
        }
    }
    
    private static int partitionByRating(List<ProductDTO> products, double[] ratings, int low, int high) {
        double pivot = ratings[high];
        int i = low - 1;
        
        for (int j = low; j < high; j++) {
            // Descending order - higher ratings first
            if (ratings[j] >= pivot) {
                i++;
                swap(products, i, j);
                swap(ratings, i, j);
            }
        }
        
        swap(products, i + 1, high);
        swap(ratings, i + 1, high);
        return i + 1;
    }
    
    private static void swap(List<ProductDTO> products, int i, int j) {
        ProductDTO temp = products.get(i);
        products.set(i, products.get(j));
        products.set(j, temp);
    }
    
    private static void swap(double[] array, int i, int j) {
        double temp = array[i];
        array[i] = array[j];
        array[j] = temp;
    }
}

