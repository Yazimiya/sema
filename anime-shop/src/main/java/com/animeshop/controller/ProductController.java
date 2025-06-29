package com.animeshop.controller;

import com.animeshop.entity.Product;
import com.animeshop.service.ProductService;
import com.animeshop.service.CategoryService;
import com.animeshop.service.CurrencyService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;

@Controller
public class ProductController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final CurrencyService currencyService;

    public ProductController(ProductService productService,
                             CategoryService categoryService,
                             CurrencyService currencyService) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.currencyService = currencyService;
    }

    @GetMapping("/products")
    public String getAllProducts(@RequestParam(value = "categoryId", required = false) String categoryId,
                                 @RequestParam(value = "minPrice", required = false) Double minPrice,
                                 @RequestParam(value = "maxPrice", required = false) Double maxPrice,
                                 @RequestParam(value = "currency", required = false) String currencyParam,
                                 @RequestParam(value = "sort", required = false) String sort,
                                 HttpSession session,
                                 Model model) {


        String currency = currencyParam != null ? currencyParam : (String) session.getAttribute("currency");
        if (currency == null) currency = "RUB";
        session.setAttribute("currency", currency);

        double rate = currency.equals("RUB") ? 1.0 : currencyService.getRate(currency);

        Double minRub = minPrice != null ? minPrice / rate : null;
        Double maxRub = maxPrice != null ? maxPrice / rate : null;

        List<Product> products;
        Long selectedCategoryId = null;

        if (categoryId != null && !categoryId.equalsIgnoreCase("all") && !categoryId.equals("__all__")) {
            try {
                selectedCategoryId = Long.parseLong(categoryId);
            } catch (NumberFormatException ignored) {}
        }

        if ("rating_desc".equalsIgnoreCase(sort)) {
            products = productService.findByFiltersSortedByRatingDesc(selectedCategoryId, minRub, maxRub);
        } else if ("rating_asc".equalsIgnoreCase(sort)) {
            products = productService.findByFiltersSortedByRatingAsc(selectedCategoryId, minRub, maxRub);
        } else {
            if (selectedCategoryId == null) {
                products = productService.findByPriceRange(minRub, maxRub);
            } else {
                products = productService.findByCategoryAndPrice(selectedCategoryId, minRub, maxRub);
            }
        }

        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        DecimalFormat df = new DecimalFormat("#0.00", symbols);

        String symbol = switch (currency.toUpperCase()) {
            case "USD" -> "$";
            case "EUR" -> "€";
            case "BYN" -> "BYN";
            case "CNY" -> "¥";
            case "UAH" -> "₴";
            default -> "₽";
        };

        Map<Long, String> priceMap = new HashMap<>();
        for (Product p : products) {
            priceMap.put(p.getId(), df.format(p.getPrice() * rate));
        }

        model.addAttribute("products", products);
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("selectedCategoryId", selectedCategoryId);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("currency", currency);
        model.addAttribute("symbol", symbol);
        model.addAttribute("priceMap", priceMap);
        model.addAttribute("sort", sort);

        return "products";
    }

    @GetMapping("/api/products/search")
    @ResponseBody
    public List<Product> searchProducts(@RequestParam String query) {
        return productService.searchByName(query);
    }

    @GetMapping("/api/products/search-fragment")
    public String searchFragment(@RequestParam String query, Model model) {
        List<Product> products = productService.searchByName(query);
        model.addAttribute("products", products);
        return "fragments/product-list :: productList";
    }
}
