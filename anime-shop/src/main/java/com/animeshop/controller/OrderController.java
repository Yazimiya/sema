package com.animeshop.controller;

import com.animeshop.entity.Order;
import com.animeshop.entity.User;
import com.animeshop.repository.UserRepository;
import com.animeshop.service.CurrencyService;
import com.animeshop.service.OrderService;
import jakarta.servlet.http.HttpSession;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;
    private final UserRepository userRepository;
    private final CurrencyService currencyService;

    public OrderController(OrderService orderService,
                           UserRepository userRepository,
                           CurrencyService currencyService) {
        this.orderService = orderService;
        this.userRepository = userRepository;
        this.currencyService = currencyService;
    }

    @PostMapping("/place")
    public String placeOrder(HttpSession session,
                             @AuthenticationPrincipal UserDetails userDetails) {

        @SuppressWarnings("unchecked")
        Map<Long, Integer> cart = (Map<Long, Integer>) session.getAttribute("cart");

        if (cart == null || cart.isEmpty()) {
            return "redirect:/cart";
        }

        String email = userDetails.getUsername();
        Optional<User> optionalUser = userRepository.findByEmail(email);

        if (optionalUser.isEmpty()) {
            System.err.println("❌ Пользователь не найден по email: " + email);
            return "redirect:/cart";
        }

        User user = optionalUser.get();

        try {
            orderService.placeOrder(cart, user);
            session.removeAttribute("cart");
            return "redirect:/orders/my";
        } catch (Exception e) {
            System.err.println("❌ Ошибка при оформлении заказа: " + e.getMessage());
            return "redirect:/cart";
        }
    }

    @GetMapping("/my")
    public String myOrders(Model model,
                           @AuthenticationPrincipal UserDetails userDetails,
                           HttpSession session,
                           @RequestParam(value = "currency", required = false) String currencyParam,
                           @RequestParam(value = "status", required = false) String status,
                           @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                           @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        String currency = currencyParam != null
                ? currencyParam
                : (String) session.getAttribute("currency");
        if (currency == null) currency = "RUB";
        session.setAttribute("currency", currency);

        double rate = currency.equals("RUB") ? 1.0 : currencyService.getRate(currency);
        String symbol = switch (currency.toUpperCase()) {
            case "USD" -> "$";
            case "EUR" -> "€";
            case "BYN" -> "BYN";
            case "CNY" -> "¥";
            case "UAH" -> "₴";
            default -> "₽";
        };

        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        DecimalFormat df = new DecimalFormat("#0.00", symbols);

        Optional<User> userOpt = userRepository.findByEmail(userDetails.getUsername());
        if (userOpt.isEmpty()) {
            return "redirect:/login";
        }

        User user = userOpt.get();

        Stream<Order> stream = orderService.getOrdersByUser(user).stream();

        // Фильтр по статусу
        if ("active".equalsIgnoreCase(status)) {
            stream = stream.filter(o -> !o.isCancelled());
        } else if ("cancelled".equalsIgnoreCase(status)) {
            stream = stream.filter(Order::isCancelled);
        }

        // Фильтр по датам
        if (from != null) {
            stream = stream.filter(o -> !o.getDateTime().toLocalDate().isBefore(from));
        }
        if (to != null) {
            stream = stream.filter(o -> !o.getDateTime().toLocalDate().isAfter(to));
        }

        List<Order> orders = stream.collect(Collectors.toList());

        Map<Long, String> formattedTotals = new HashMap<>();
        for (Order order : orders) {
            formattedTotals.put(order.getId(), df.format(order.getTotalPrice() * rate));
        }

        model.addAttribute("orders", orders);
        model.addAttribute("formattedTotals", formattedTotals);
        model.addAttribute("currency", currency);
        model.addAttribute("symbol", symbol);

        // Для формы фильтров
        model.addAttribute("filterStatus", status);
        model.addAttribute("filterFrom", from);
        model.addAttribute("filterTo", to);

        return "order/my-orders";
    }

    @PostMapping("/cancel/{orderId}")
    public String cancelOrder(@PathVariable Long orderId,
                              @AuthenticationPrincipal UserDetails userDetails) {
        orderService.cancelOrder(orderId, userDetails.getUsername());
        return "redirect:/orders/my";
    }
}
