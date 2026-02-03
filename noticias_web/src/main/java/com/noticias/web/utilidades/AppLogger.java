package com.noticias.web.utilidades;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Utilidad de logging personalizada para Noticias App (Web).
 * Proporciona un formato limpio y trazas de error filtradas.
 */
public class AppLogger {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String APP_PACKAGE = "com.noticias";

    /**
     * Registra una actividad en la aplicación.
     */
    public static void logActivity(String message) {
        String timestamp = LocalDateTime.now().format(formatter);
        System.out.println(String.format("[%s] [ACTIVIDAD] %s", timestamp, message));
    }

    /**
     * Registra un error con su mensaje y una traza filtrada.
     */
    public static void logError(String message, Throwable throwable) {
        String timestamp = LocalDateTime.now().format(formatter);
        System.err.println(String.format("[%s] [ERROR] %s: %s", timestamp, message, throwable.getMessage()));

        // Filtrar y mostrar solo las líneas de la traza que pertenecen a nuestra
        // aplicación
        String filteredStackTrace = Arrays.stream(throwable.getStackTrace())
                .filter(element -> element.getClassName().startsWith(APP_PACKAGE))
                .map(element -> String.format("    at %s.%s(%s:%d)",
                        element.getClassName(),
                        element.getMethodName(),
                        element.getFileName(),
                        element.getLineNumber()))
                .collect(Collectors.joining("\n"));

        if (!filteredStackTrace.isEmpty()) {
            System.err.println("Traza relevante:");
            System.err.println(filteredStackTrace);
        }
        System.err.println("--------------------------------------------------");
    }
}
