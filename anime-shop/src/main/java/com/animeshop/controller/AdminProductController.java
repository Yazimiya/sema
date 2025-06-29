package com.animeshop.controller;

import com.animeshop.entity.Product;
import com.animeshop.entity.Category;
import com.animeshop.service.CategoryService;
import com.animeshop.service.ProductService;
import com.animeshop.service.OrderServiceImpl.ProductCancelStats;
import com.animeshop.service.OrderService;
import com.animeshop.service.CurrencyService;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

@Controller
@RequestMapping("/admin/products")
public class AdminProductController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final OrderService orderService;
    private final CurrencyService currencyService;

    @Value("${upload.dir}")
    private String uploadDir;

    public AdminProductController(ProductService productService,
                                  CategoryService categoryService,
                                  OrderService orderService,
                                  CurrencyService currencyService) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.orderService = orderService;
        this.currencyService = currencyService;
    }

    @GetMapping
    public String list(@RequestParam(value = "currency", required = false) String currencyParam,
                       HttpSession session,
                       Model model) {

        // Выбор валюты
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

        List<Product> products = productService.findAll();
        Map<Long, ProductCancelStats> statsMap = orderService.getCancelStatsByProduct();

        model.addAttribute("products", products);
        model.addAttribute("cancelStats", statsMap);
        model.addAttribute("rate", rate);
        model.addAttribute("symbol", symbol);
        model.addAttribute("currency", currency);

        return "admin/products/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", getSortedCategories());
        return "admin/products/form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute Product product,
                       @RequestParam("image") MultipartFile image) throws IOException {

        if (image != null && !image.isEmpty()) {
            String filename = System.currentTimeMillis() + "_" + image.getOriginalFilename();
            File dir = new File(uploadDir);
            if (!dir.exists()) dir.mkdirs();
            image.transferTo(Paths.get(uploadDir, filename));
            product.setImageFilename(filename);
        }

        productService.save(product);
        return "redirect:/admin/products";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        Product product = productService.findById(id);
        model.addAttribute("product", product);
        model.addAttribute("categories", getSortedCategories());
        return "admin/products/form";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        productService.deleteById(id);
        return "redirect:/admin/products";
    }

    private List<Category> getSortedCategories() {
        return categoryService.findAll()
                .stream()
                .sorted(Comparator.comparing(Category::getName))
                .toList();
    }
}
