package com.noticias.web.seguridad;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro que redirige a usuarios con flag FORCE_RESET a la pantalla de cambio
 * de contraseña.
 * Importante: normaliza la ruta quitando el contextPath para comparar
 * correctamente.
 */
@Component
public class ForceResetFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        // Quitamos el contextPath para comparar las rutas relativas correctas
        String context = request.getContextPath(); // por ejemplo "/backend"
        String uri = request.getRequestURI(); // por ejemplo "/backend/auth/login"
        String relative = uri;
        if (context != null && !context.isEmpty() && uri.startsWith(context)) {
            relative = uri.substring(context.length()); // por ejemplo "/auth/login"
        }

        // Rutas públicas relativas (sin contextPath)
        boolean isPublicAuth = relative.equals("/auth/login")
                || relative.startsWith("/auth/")
                || relative.startsWith("/css/")
                || relative.startsWith("/js/")
                || relative.startsWith("/images/")
                || relative.startsWith("/static/")
                || relative.equals("/logout");

        HttpSession session = request.getSession(false);
        boolean forceReset = false;
        if (session != null) {
            Object attr = session.getAttribute("FORCE_RESET");
            if (attr instanceof Boolean) {
                forceReset = (Boolean) attr;
            }
        }

        // Si el usuario debe forzar cambio y no está en ruta pública, redirigir
        if (forceReset && !isPublicAuth) {
            // Redirigimos a la ruta relativa bajo el contextPath
            response.sendRedirect(request.getContextPath() + "/auth/force-change-password");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
