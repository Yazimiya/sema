package com.animeshop.controller;

import com.animeshop.entity.Product;
import com.animeshop.entity.Rating;
import com.animeshop.entity.User;
import com.animeshop.repository.ProductRepository;
import com.animeshop.repository.RatingRepository;
import com.animeshop.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.HashMap;

import java.util.Optional;

@RestController
@RequestMapping("/api/rating")
public class RatingController {

    private final RatingRepository ratingRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public RatingController(RatingRepository ratingRepository,
                            ProductRepository productRepository,
                            UserRepository userRepository) {
        this.ratingRepository = ratingRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/rate")
    public ResponseEntity<?> rateProduct(
            @RequestParam Long productId,
            @RequestParam int value,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (value < 1 || value > 5) {
            return ResponseEntity.badRequest().body("Рейтинг должен быть от 1 до 5");
        }

        if (userDetails == null) {
            return ResponseEntity.status(401).body("Вы не авторизованы");
        }

        Optional<User> userOpt = userRepository.findByEmail(userDetails.getUsername());
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Пользователь не найден");
        }
        User user = userOpt.get();

        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Товар не найден");
        }
        Product product = productOpt.get();

        Rating rating = ratingRepository.findByProductAndUser(product, user)
                .orElse(new Rating());

        rating.setProduct(product);
        rating.setUser(user);
        rating.setScore(value);

        ratingRepository.save(rating);

        double average = ratingRepository.getAverageRatingForProduct(product.getId());
        return ResponseEntity.ok(average);
    }

    @GetMapping("/average")
    public ResponseEntity<Map<String, Object>> getAverageRating(@RequestParam Long productId) {
        double avg = ratingRepository.getAverageRatingForProduct(productId);
        int count = ratingRepository.countRatingsByProductId(productId);

        Map<String, Object> result = new HashMap<>();
        result.put("average", avg);
        result.put("count", count);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/user-rating")
    public ResponseEntity<?> getUserRating(@RequestParam Long productId,
                                           @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        Optional<User> userOpt = userRepository.findByEmail(userDetails.getUsername());
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Пользователь не найден");
        }

        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Товар не найден");
        }

        Optional<Rating> ratingOpt = ratingRepository.findByProductAndUser(productOpt.get(), userOpt.get());
        if (ratingOpt.isPresent()) {
            return ResponseEntity.ok(ratingOpt.get().getScore());
        } else {
            return ResponseEntity.ok(null);
        }
    }

}
