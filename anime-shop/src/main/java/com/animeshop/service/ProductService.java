package com.animeshop.service;

import com.animeshop.entity.Product;

import java.util.List;

public interface ProductService {
    List<Product> findAll();
    Product findById(Long id);
    Product save(Product product);
    void deleteById(Long id);
    List<Product> findByCategoryId(Long categoryId);
    List<Product> findByPriceRange(Double min, Double max);
    List<Product> findByCategoryAndPrice(Long categoryId, Double min, Double max);
    List<Product> searchByName(String query);
    List<Product> findAllById(Iterable<Long> ids);

    List<Product> findAllSortedByRatingDesc();
    List<Product> findAllSortedByRatingAsc();
    List<Product> findByFiltersSortedByRatingDesc(Long categoryId, Double min, Double max);
    List<Product> findByFiltersSortedByRatingAsc(Long categoryId, Double min, Double max);

    double getCancelRateForProduct(Long productId);
}
