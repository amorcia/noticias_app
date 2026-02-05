package com.noticias.web.interceptores;

import com.noticias.web.servicios.LoggingServicio;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * Interceptor para logging automático de acciones de usuario
 */
@Component
public class LoggingInterceptor implements HandlerInterceptor {

    private final LoggingServicio loggingServicio;

    public LoggingInterceptor(LoggingServicio loggingServicio) {
        this.loggingServicio = loggingServicio;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        // Guardar tiempo de inicio para medir duración
        request.setAttribute("startTime", System.currentTimeMillis());
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
            ModelAndView modelAndView) throws Exception {
        // No hacer nada aquí, esperamos a afterCompletion
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
            Exception ex) throws Exception {

        // Obtener información de la sesión
        HttpSession session = request.getSession(false);
        if (session == null) {
            return; // No hay sesión, no loguear
        }

        Integer usuarioId = (Integer) session.getAttribute("usuarioId");
        if (usuarioId == null) {
            return; // Usuario no autenticado, no loguear
        }

        // Información de la petición
        String ruta = request.getRequestURI();
        String metodo = request.getMethod();
        int statusCode = response.getStatus();

        // Calcular duración
        Long startTime = (Long) request.getAttribute("startTime");
        long duracion = startTime != null ? System.currentTimeMillis() - startTime : 0;

        // Determinar el handler (controlador y método)
        String controlador = "Unknown";
        String metodoHandler = "Unknown";
        String paquete = "Unknown";

        if (handler != null) {
            String handlerStr = handler.toString();
            // Extraer información del handler
            // Formato típico: "com.noticias.web.controladores.AuthControlador#login()"
            if (handlerStr.contains("#")) {
                String[] parts = handlerStr.split("#");
                if (parts.length >= 2) {
                    String fullClass = parts[0];
                    metodoHandler = parts[1].replace("()", "");

                    // Extraer paquete y clase
                    int lastDot = fullClass.lastIndexOf('.');
                    if (lastDot > 0) {
                        paquete = fullClass.substring(0, lastDot);
                        controlador = fullClass.substring(lastDot + 1);
                    } else {
                        controlador = fullClass;
                    }
                }
            }
        }

        // Determinar resultado
        String resultado = statusCode >= 200 && statusCode < 300 ? "SUCCESS"
                : statusCode >= 400 && statusCode < 500 ? "CLIENT_ERROR"
                        : statusCode >= 500 ? "SERVER_ERROR" : "UNKNOWN";

        // Construir mensaje descriptivo
        String mensaje = String.format("%s %s -> %d (%dms)", metodo, ruta, statusCode, duracion);

        // Si hubo excepción, agregar al mensaje
        if (ex != null) {
            mensaje += " | Error: " + ex.getMessage();
            resultado = "ERROR";
        }

        // Loguear la acción
        String accion = determinarAccion(metodo, ruta);
        loggingServicio.logAccionSesion(
                usuarioId,
                accion,
                ruta,
                metodo,
                controlador + "." + metodoHandler,
                paquete,
                resultado,
                mensaje);

        // Si hubo error, también loguear en archivo de errores
        if (ex != null) {
            loggingServicio.logErrorToFile(
                    "INTERCEPTOR_ERROR",
                    ex.getMessage(),
                    this.getClass().getName(),
                    "afterCompletion",
                    "com.noticias.web.interceptores",
                    ruta,
                    ex);
        }
    }

    /**
     * Determina la acción basándose en el método HTTP y la ruta
     */
    private String determinarAccion(String metodo, String ruta) {
        // Acciones específicas basadas en rutas conocidas
        if (ruta.contains("/login"))
            return "LOGIN";
        if (ruta.contains("/logout"))
            return "LOGOUT";
        if (ruta.contains("/registro"))
            return "REGISTRO";
        if (ruta.contains("/perfil"))
            return "VER_PERFIL";
        if (ruta.contains("/admin"))
            return "ACCESO_ADMIN";
        if (ruta.contains("/noticias/crear"))
            return "CREAR_NOTICIA";
        if (ruta.contains("/noticias/editar"))
            return "EDITAR_NOTICIA";
        if (ruta.contains("/noticias/eliminar"))
            return "ELIMINAR_NOTICIA";
        if (ruta.contains("/usuarios/eliminar"))
            return "ELIMINAR_USUARIO";
        if (ruta.contains("/comentarios"))
            return "COMENTAR";
        if (ruta.contains("/votar"))
            return "VOTAR";

        // Acciones genéricas basadas en método HTTP
        switch (metodo) {
            case "GET":
                return "VER";
            case "POST":
                return "CREAR";
            case "PUT":
                return "ACTUALIZAR";
            case "DELETE":
                return "ELIMINAR";
            default:
                return "ACCION";
        }
    }
}
