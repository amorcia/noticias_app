package com.noticias.web.seguridad;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private static final String DEFAULT_PASSWORD = "ChangeMe123!"; // Contraseña predeterminada

    public CustomAuthenticationSuccessHandler() {
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        // Intentamos verificar si la contraseña usada es la predeterminada.
        // Nota: authentication.getCredentials() puede ser null si se borraron las
        // credenciales.
        // Si es null, no podemos verificarlo aquí fácilmente sin cambiar la
        // configuración de seguridad.
        // Asumimos que si podemos acceder, lo comprobamos.

        boolean isDefaultPassword = false;
        if (authentication.getCredentials() != null) {
            String rawPassword = authentication.getCredentials().toString();
            if (DEFAULT_PASSWORD.equals(rawPassword)) {
                isDefaultPassword = true;
            }
        }

        // Lógica alternativa: Si no podemos acceder a credentials,
        // podríamos tener un flag en el usuario (UserDetails) si viniera de base de
        // datos.
        // Como no tenemos flag en DTO, dependemos de credentials o de que el Admin lo
        // gestione.

        // Si es contraseña por defecto, activamos flag en sesión
        if (isDefaultPassword) {
            HttpSession session = request.getSession();
            session.setAttribute("FORCE_RESET", true);
            response.sendRedirect(request.getContextPath() + "/auth/force-change-password");
        } else {
            // Limpiar flag por si acaso
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.removeAttribute("FORCE_RESET");
            }
            response.sendRedirect(request.getContextPath() + "/menu");
        }
    }
}
