package com.noticias.web.seguridad;

import com.noticias.web.dtos.UsuarioDTO;
import com.noticias.web.servicios.ApiNoticiasCliente;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtFiltro extends OncePerRequestFilter {

    private final ApiNoticiasCliente apiCliente;

    public JwtFiltro(ApiNoticiasCliente apiCliente) {
        this.apiCliente = apiCliente;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String requestURI = request.getRequestURI();

        boolean isStaticResource = requestURI.contains("/css/") || requestURI.contains("/js/") ||
                requestURI.contains("/images/") || requestURI.contains("/static/");

        if (!isStaticResource) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                UsuarioDTO usuarioSession = (UsuarioDTO) session.getAttribute("usuario");

                if (usuarioSession != null && usuarioSession.getTokenSession() != null) {
                    try {
                        // Verificación estricta contra backend (DB)
                        // Llamamos a la API para ver si el token de sesión coincide
                        UsuarioDTO usuarioDb = apiCliente.buscarUsuarioPorId(usuarioSession.getId());

                        // Si usuario existe, tiene token de sesión y coincide con el de la sesión
                        // actual
                        if (usuarioDb != null &&
                                usuarioDb.getTokenSession() != null &&
                                usuarioDb.getTokenSession().equals(usuarioSession.getTokenSession())) {

                            // Sesión válida: Establecer contexto de seguridad Spring Security
                            String rol = usuarioDb.getRolNombre();
                            if (rol == null || rol.isBlank()) {
                                rol = "USER";
                            }

                            SimpleGrantedAuthority autoridad = new SimpleGrantedAuthority(rol);
                            Authentication auth = new UsernamePasswordAuthenticationToken(
                                    usuarioDb.getEmail(),
                                    null,
                                    List.of(autoridad));

                            SecurityContextHolder.getContext().setAuthentication(auth);

                        } else {
                            // Token inválido o sesión cerrada en otro lado -> Invalidar sesión actual
                            session.invalidate();
                            SecurityContextHolder.clearContext();
                        }
                    } catch (Exception ex) {
                        // Error al validar -> Invalidar por seguridad
                        session.invalidate();
                        SecurityContextHolder.clearContext();
                    }
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
