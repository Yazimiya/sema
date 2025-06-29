package com.animeshop.controller;

import com.animeshop.entity.Order;
import com.animeshop.service.CurrencyService;
import com.animeshop.service.OrderService;
import jakarta.servlet.http.HttpSession;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller
public class AdminOrderController {

    private final OrderService orderService;
    private final CurrencyService currencyService;

    public AdminOrderController(OrderService orderService,
                                CurrencyService currencyService) {
        this.orderService = orderService;
        this.currencyService = currencyService;
    }

    @GetMapping("/admin/orders")
    public String listOrders(
            @RequestParam(value = "currency", required = false) String currencyParam,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            HttpSession session,
            Model model) {

        // Валюта
        String currency = currencyParam != null ? currencyParam : (String) session.getAttribute("currency");
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

        // Получение всех заказов
        List<Order> allOrders = orderService.findAll();

        // Применение фильтров
        Stream<Order> stream = allOrders.stream();

        if (email != null && !email.isBlank()) {
            stream = stream.filter(o -> o.getUser().getEmail().toLowerCase().contains(email.toLowerCase()));
        }

        if ("active".equalsIgnoreCase(status)) {
            stream = stream.filter(o -> !o.isCancelled());
        } else if ("cancelled".equalsIgnoreCase(status)) {
            stream = stream.filter(Order::isCancelled);
        }

        if (from != null) {
            stream = stream.filter(o -> !o.getDateTime().toLocalDate().isBefore(from));
        }

        if (to != null) {
            stream = stream.filter(o -> !o.getDateTime().toLocalDate().isAfter(to));
        }

        List<Order> orders = stream.collect(Collectors.toList());

        long total = orders.size();
        long cancelled = orders.stream().filter(Order::isCancelled).count();
        double percent = total > 0 ? ((double) cancelled / total) * 100 : 0.0;

        model.addAttribute("orders", orders);
        model.addAttribute("total", total);
        model.addAttribute("cancelled", cancelled);
        model.addAttribute("cancelPercent", String.format("%.2f", percent));
        model.addAttribute("currency", currency);
        model.addAttribute("rate", rate);
        model.addAttribute("symbol", symbol);

        // для отображения в форме
        model.addAttribute("filterEmail", email);
        model.addAttribute("filterStatus", status);
        model.addAttribute("filterFrom", from);
        model.addAttribute("filterTo", to);

        return "admin/orders";
    }

    @PostMapping("/admin/orders/{id}/cancel")
    public String cancelOrder(@PathVariable Long id) {
        orderService.cancelByAdmin(id);
        return "redirect:/admin/orders";
    }

    @PostMapping("/admin/orders/{id}/restore")
    public String restoreOrder(@PathVariable Long id) {
        orderService.restoreByAdmin(id);
        return "redirect:/admin/orders";
    }

    @GetMapping("/admin/orders/{id}")
    public String viewOrder(@PathVariable Long id,
                            @RequestParam(value = "currency", required = false) String currencyParam,
                            HttpSession session,
                            Model model) {

        String currency = currencyParam != null ? currencyParam : (String) session.getAttribute("currency");
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

        Order order = orderService.findById(id);
        if (order == null) {
            model.addAttribute("message", "Заказ не найден");
            return "error";
        }

        model.addAttribute("order", order);
        model.addAttribute("items", order.getItems());
        model.addAttribute("currency", currency);
        model.addAttribute("rate", rate);
        model.addAttribute("symbol", symbol);

        return "admin/order-details";
    }
}
