package com.animeshop.service;

import com.animeshop.entity.Product;
import com.animeshop.repository.ProductRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class CartService {

    private final ProductRepository productRepository;

    public CartService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @SuppressWarnings("unchecked")
    public Map<Long, Integer> getCart(HttpSession session) {
        Map<Long, Integer> cart = (Map<Long, Integer>) session.getAttribute("cart");
        if (cart == null) {
            cart = new HashMap<>();
            session.setAttribute("cart", cart);
        }
        return cart;
    }

    public void addToCart(Long productId, HttpSession session) {
        Map<Long, Integer> cart = getCart(session);
        cart.put(productId, cart.getOrDefault(productId, 0) + 1);
        session.setAttribute("cart", cart);
    }

    public void removeFromCart(Long productId, HttpSession session) {
        Map<Long, Integer> cart = getCart(session);
        cart.remove(productId);
        session.setAttribute("cart", cart);
    }

    public void clearCart(HttpSession session) {
        session.removeAttribute("cart");
    }

    public Map<Product, Integer> getProductsInCart(HttpSession session) {
        Map<Product, Integer> productMap = new HashMap<>();
        Map<Long, Integer> cart = getCart(session);

        for (Map.Entry<Long, Integer> entry : cart.entrySet()) {
            productRepository.findById(entry.getKey()).ifPresent(product ->
                    productMap.put(product, entry.getValue()));
        }

        return productMap;
    }
}
