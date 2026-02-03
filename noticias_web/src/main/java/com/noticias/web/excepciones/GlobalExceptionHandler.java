package com.noticias.web.excepciones;

import com.noticias.web.utilidades.AppLogger;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Manejador global de excepciones para el módulo Web.
 * Captura errores, los registra con AppLogger y muestra la vista de error.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public String handleException(Exception ex, Model model, HttpServletRequest request) {
        AppLogger.logError("Error inesperado en la interfaz Web", ex);
        model.addAttribute("error", "Ha ocurrido un error inesperado: " + ex.getMessage());
        model.addAttribute("url", request.getRequestURL());
        return "error/general";
    }

    @ExceptionHandler(RuntimeException.class)
    public String handleRuntimeException(RuntimeException ex, Model model, HttpServletRequest request) {
        AppLogger.logError("Error de lógica o conexión en la Web", ex);
        model.addAttribute("error", ex.getMessage());
        model.addAttribute("url", request.getRequestURL());
        return "error/general";
    }
}
