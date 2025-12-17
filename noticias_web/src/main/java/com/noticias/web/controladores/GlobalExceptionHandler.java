package com.noticias.web.controladores;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public String handleException(Exception ex, Model model, HttpServletRequest request) {
        ex.printStackTrace(); // Log to console
        model.addAttribute("error", "Ocurrió un error inesperado: " + ex.getMessage());
        model.addAttribute("url", request.getRequestURL());
        return "error/general"; // Template moved to templates/error/general.html
    }
}
