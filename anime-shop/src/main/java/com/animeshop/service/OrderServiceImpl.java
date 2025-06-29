package com.animeshop.service;

import com.animeshop.entity.Order;
import com.animeshop.entity.OrderItem;
import com.animeshop.entity.Product;
import com.animeshop.entity.User;
import com.animeshop.repository.OrderRepository;
import com.animeshop.repository.ProductRepository;
import com.animeshop.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class OrderServiceImpl implements OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public OrderServiceImpl(OrderRepository orderRepository,
                            ProductRepository productRepository,
                            UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @PostConstruct
    public void testOrderLog() {
        logger.error(">>> Тестовая ошибка из OrderServiceImpl (для order-errors.log)");
    }

    @Override
    public Order placeOrder(Map<Long, Integer> cart, User user) {
        if (cart == null || cart.isEmpty()) {
            logger.warn("Попытка оформить заказ с пустой корзиной");
            throw new IllegalArgumentException("Корзина пуста");
        }

        Order order = new Order();
        order.setUser(user);
        order.setDateTime(LocalDateTime.now());
        order.setCancelled(false);

        List<OrderItem> items = new ArrayList<>();
        double total = 0;

        for (Map.Entry<Long, Integer> entry : cart.entrySet()) {
            Product product = productRepository.findById(entry.getKey())
                    .orElseThrow(() -> {
                        logger.error("Товар с id {} не найден", entry.getKey());
                        return new RuntimeException("Товар не найден");
                    });
            int quantity = entry.getValue();

            OrderItem item = new OrderItem(order, product, quantity);
            items.add(item);
            total += product.getPrice() * quantity;
        }

        order.setItems(items);
        order.setTotalPrice(total);

        return orderRepository.save(order);
    }

    @Override
    public List<Order> getOrdersByUser(User user) {
        return orderRepository.findByUser(user);
    }

    @Override
    public void cancelOrder(Long orderId, String email) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    logger.error("Не удалось найти заказ id={} для отмены", orderId);
                    return new RuntimeException("Заказ не найден");
                });

        if (order.getUser().getEmail().equals(email)) {
            order.setCancelled(true);
            orderRepository.save(order);
        } else {
            logger.warn("Попытка отменить чужой заказ пользователем {}", email);
            throw new SecurityException("Попытка отмены чужого заказа");
        }
    }

    @Override
    public List<Order> findAll() {
        return orderRepository.findAll();
    }

    @Override
    public void cancelByAdmin(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    logger.error("Заказ id={} не найден для отмены админом", orderId);
                    return new RuntimeException("Заказ не найден");
                });
        order.setCancelled(true);
        orderRepository.save(order);
    }

    @Override
    public void restoreByAdmin(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    logger.error("Заказ id={} не найден для восстановления админом", orderId);
                    return new RuntimeException("Заказ не найден");
                });
        order.setCancelled(false);
        orderRepository.save(order);
    }

    @Override
    public Order findById(Long id) {
        return orderRepository.findById(id).orElse(null);
    }

    @Override
    public Map<Long, ProductCancelStats> getCancelStatsByProduct() {
        List<Order> all = orderRepository.findAll();
        Map<Long, Integer> totalByProduct = new HashMap<>();
        Map<Long, Integer> cancelledByProduct = new HashMap<>();

        for (Order order : all) {
            for (OrderItem item : order.getItems()) {
                Long productId = item.getProduct().getId();
                totalByProduct.merge(productId, 1, Integer::sum);
                if (order.isCancelled()) {
                    cancelledByProduct.merge(productId, 1, Integer::sum);
                }
            }
        }

        Map<Long, ProductCancelStats> result = new HashMap<>();
        for (Long productId : totalByProduct.keySet()) {
            int total = totalByProduct.getOrDefault(productId, 0);
            int cancelled = cancelledByProduct.getOrDefault(productId, 0);
            double percent = total > 0 ? (cancelled * 100.0 / total) : 0.0;
            result.put(productId, new ProductCancelStats(total, cancelled, percent));
        }

        return result;
    }

    public static class ProductCancelStats {
        private final int total;
        private final int cancelled;
        private final double percent;

        public ProductCancelStats(int total, int cancelled, double percent) {
            this.total = total;
            this.cancelled = cancelled;
            this.percent = percent;
        }

        public int getTotal() {
            return total;
        }

        public int getCancelled() {
            return cancelled;
        }

        public double getPercent() {
            return percent;
        }
    }
}
