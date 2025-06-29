package com.animeshop.controller;

import com.animeshop.entity.Product;
import com.animeshop.service.CartService;
import com.animeshop.service.CurrencyService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;

@Controller
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;
    private final CurrencyService currencyService;

    public CartController(CartService cartService, CurrencyService currencyService) {
        this.cartService = cartService;
        this.currencyService = currencyService;
    }

    @GetMapping
    public String viewCart(Model model,
                           HttpSession session,
                           @RequestParam(value = "currency", required = false) String currencyParam) {

        String currency = currencyParam != null ? currencyParam : (String) session.getAttribute("currency");
        if (currency == null) currency = "RUB";
        session.setAttribute("currency", currency);

        double rate = currency.equals("RUB") ? 1.0 : currencyService.getRate(currency);
        String symbol = switch (currency) {
            case "USD" -> "$";
            case "EUR" -> "€";
            case "BYN" -> "BYN";
            case "CNY" -> "¥";
            case "UAH" -> "₴";
            default -> "₽";
        };

        DecimalFormat df = new DecimalFormat("#0.00", new DecimalFormatSymbols(Locale.US));

        Map<Product, Integer> productsInCart = cartService.getProductsInCart(session);

        List<Product> products = new ArrayList<>(productsInCart.keySet());
        Map<Long, String> priceMap = new HashMap<>();
        Map<Long, String> subtotalMap = new HashMap<>();
        Map<Long, Integer> quantities = new HashMap<>();

        double total = 0;

        for (Product product : products) {
            Long id = product.getId();
            int quantity = productsInCart.get(product);
            double convertedPrice = product.getPrice() * rate;
            double subtotal = convertedPrice * quantity;

            priceMap.put(id, df.format(convertedPrice));
            subtotalMap.put(id, df.format(subtotal));
            quantities.put(id, quantity);
            total += subtotal;
        }

        model.addAttribute("products", products);
        model.addAttribute("priceMap", priceMap);
        model.addAttribute("subtotalMap", subtotalMap);
        model.addAttribute("quantities", quantities);
        model.addAttribute("total", df.format(total));
        model.addAttribute("symbol", symbol);
        model.addAttribute("currency", currency);

        return "cart/view";
    }

    @PostMapping("/add/{productId}")
    public String addToCart(@PathVariable Long productId, HttpSession session) {
        cartService.addToCart(productId, session);
        return "redirect:/cart";
    }

    @PostMapping("/remove/{productId}")
    public String removeFromCart(@PathVariable Long productId, HttpSession session) {
        cartService.removeFromCart(productId, session);
        return "redirect:/cart";
    }

    @PostMapping("/clear")
    public String clearCart(HttpSession session) {
        cartService.clearCart(session);
        return "redirect:/cart";
    }
}
