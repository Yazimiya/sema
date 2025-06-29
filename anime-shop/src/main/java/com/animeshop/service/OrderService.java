package com.animeshop.service;

import com.animeshop.entity.Order;
import com.animeshop.entity.User;

import java.util.List;
import java.util.Map;

public interface OrderService {
    Order placeOrder(Map<Long, Integer> cart, User user);
    List<Order> getOrdersByUser(User user);
    void cancelOrder(Long orderId, String email);
    List<Order> findAll();
    void cancelByAdmin(Long orderId);
    void restoreByAdmin(Long orderId);
    Order findById(Long id);
    Map<Long, OrderServiceImpl.ProductCancelStats> getCancelStatsByProduct();


}
