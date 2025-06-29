package com.animeshop.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object statusCode = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

        if (statusCode != null) {
            int status = Integer.parseInt(statusCode.toString());

            if (status == HttpServletResponse.SC_FORBIDDEN) {
                return "error-403";
            } else if (status == HttpServletResponse.SC_NOT_FOUND) {
                return "error-404";
            } else if (status == HttpServletResponse.SC_INTERNAL_SERVER_ERROR) {
                return "error-500";
            } else {
                model.addAttribute("message", "Неизвестная ошибка: " + status);
                return "error";
            }
        }

        model.addAttribute("message", "Неопределённая ошибка.");
        return "error";
    }
}
