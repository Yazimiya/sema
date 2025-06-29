package com.animeshop.repository;

import com.animeshop.entity.Product;
import com.animeshop.entity.Rating;
import com.animeshop.entity.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RatingRepository extends CrudRepository<Rating, Long> {

    Optional<Rating> findByProductAndUser(Product product, User user);

    @Query("SELECT COALESCE(AVG(r.score), 0) FROM Rating r WHERE r.product.id = :productId")
    double getAverageRatingForProduct(@Param("productId") Long productId);

    @Query("SELECT COUNT(r) FROM Rating r WHERE r.product.id = :productId")
    int countRatingsByProductId(@Param("productId") Long productId);
}
