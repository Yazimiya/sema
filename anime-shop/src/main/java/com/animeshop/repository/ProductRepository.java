package com.animeshop.repository;

import com.animeshop.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.category")
    List<Product> findAllWithCategory();

    List<Product> findByCategoryId(Long categoryId);

    List<Product> findByPriceBetween(Double min, Double max);

    List<Product> findByCategoryIdAndPriceBetween(Long categoryId, Double min, Double max);

    @Query("""
        SELECT DISTINCT p FROM Product p 
        LEFT JOIN FETCH p.category 
        WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%'))
    """)
    List<Product> searchByName(@Param("query") String query);

    //Все товары: рейтинг по убыванию (лучшие сверху)
    @Query("""
        SELECT p FROM Product p
        LEFT JOIN Rating r ON r.product = p
        GROUP BY p
        ORDER BY AVG(r.score) DESC
    """)
    List<Product> findAllOrderByAverageRatingDesc();

    // Все товары: рейтинг по возрастанию (худшие сверху)
    @Query("""
        SELECT p FROM Product p
        LEFT JOIN Rating r ON r.product = p
        GROUP BY p
        ORDER BY AVG(r.score) ASC
    """)
    List<Product> findAllOrderByAverageRatingAsc();

    // Фильтрация + рейтинг по убыванию
    @Query("""
        SELECT p FROM Product p
        LEFT JOIN Rating r ON r.product = p
        WHERE (:categoryId IS NULL OR p.category.id = :categoryId)
          AND (:min IS NULL OR p.price >= :min)
          AND (:max IS NULL OR p.price <= :max)
        GROUP BY p
        ORDER BY AVG(r.score) DESC
    """)
    List<Product> findByFiltersSortedByRatingDesc(@Param("categoryId") Long categoryId,
                                                  @Param("min") Double min,
                                                  @Param("max") Double max);

    // Фильтрация + рейтинг по возрастанию
    @Query("""
        SELECT p FROM Product p
        LEFT JOIN Rating r ON r.product = p
        WHERE (:categoryId IS NULL OR p.category.id = :categoryId)
          AND (:min IS NULL OR p.price >= :min)
          AND (:max IS NULL OR p.price <= :max)
        GROUP BY p
        ORDER BY AVG(r.score) ASC
    """)
    List<Product> findByFiltersSortedByRatingAsc(@Param("categoryId") Long categoryId,
                                                 @Param("min") Double min,
                                                 @Param("max") Double max);
}
