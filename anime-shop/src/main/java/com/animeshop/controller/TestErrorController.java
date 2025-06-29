package com.animeshop.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;

@Controller
public class TestErrorController {

    @GetMapping("/test-errors")
    public String testPage() {
        return "test-errors";
    }

    @GetMapping("/test-403")
    public void trigger403(HttpServletResponse response) throws IOException {
        response.sendError(HttpServletResponse.SC_FORBIDDEN);
    }

    @GetMapping("/test-404")
    public void trigger404(HttpServletResponse response) throws IOException {
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    @GetMapping("/test-500")
    public String trigger500() {
        throw new RuntimeException("Тестовая ошибка 500");
    }

    @GetMapping("/test-unknown")
    public void triggerUnknown(HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setStatus(418); // нестандартный код
        request.setAttribute(RequestDispatcher.ERROR_STATUS_CODE, 418);
        request.getRequestDispatcher("/error").forward(request, response);
    }
}
