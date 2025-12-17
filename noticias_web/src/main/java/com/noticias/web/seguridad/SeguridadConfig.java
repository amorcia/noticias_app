package com.noticias.web.seguridad;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuración de seguridad principal.
 * Usa controlador personalizado para autenticación en lugar de Spring Security
 * form login.
 */
@Configuration
@EnableWebSecurity
public class SeguridadConfig {

        private final JwtFiltro jwtFiltro;
        private final com.noticias.web.servicios.ApiNoticiasCliente apiCliente;

        public SeguridadConfig(JwtFiltro jwtFiltro, com.noticias.web.servicios.ApiNoticiasCliente apiCliente) {
                this.jwtFiltro = jwtFiltro;
                this.apiCliente = apiCliente;
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .csrf(csrf -> csrf.disable())
                                .authorizeHttpRequests(auth -> auth
                                                // Recursos estáticos públicos
                                                .requestMatchers("/css/**", "/js/**", "/images/**", "/static/**",
                                                                "/error")
                                                .permitAll()

                                                // Páginas públicas
                                                .requestMatchers("/", "/inicio", "/home").permitAll()
                                                .requestMatchers("/noticia/**", "/categoria/**", "/noticias/**",
                                                                "/vuestras-noticias/**", "/foro/**")
                                                .permitAll()

                                                // Autenticación pública
                                                .requestMatchers("/auth/**").permitAll()

                                                // Panel de administración - Solo ADMIN u OWNER
                                                .requestMatchers("/admin/**")
                                                .hasAnyAuthority("ADMIN", "OWNER", "Admin", "Owner")

                                                // Comentarios - Requiere autenticación
                                                .requestMatchers("/comentario/**").authenticated()

                                                // Cualquier otra ruta requiere autenticación
                                                .anyRequest().authenticated())
                                // Deshabilitar formLogin por defecto para usar nuestro controlador
                                .formLogin(form -> form.disable())
                                .logout(logout -> logout
                                                .logoutUrl("/auth/logout")
                                                .addLogoutHandler((request, response, authentication) -> {
                                                        try {
                                                                jakarta.servlet.http.HttpSession session = request
                                                                                .getSession(false);
                                                                if (session != null) {
                                                                        com.noticias.web.dtos.UsuarioDTO usuario = (com.noticias.web.dtos.UsuarioDTO) session
                                                                                        .getAttribute("usuario");
                                                                        if (usuario != null
                                                                                        && usuario.getId() != null) {
                                                                                apiCliente.cerrarSesion(
                                                                                                usuario.getId());
                                                                                System.out.println(
                                                                                                "✅ Token de sesión eliminado para usuario ID: "
                                                                                                                + usuario.getId());
                                                                        }
                                                                }
                                                        } catch (Exception e) {
                                                                System.err.println("❌ Error al cerrar sesión en API: "
                                                                                + e.getMessage());
                                                        }
                                                })
                                                .logoutSuccessUrl("/")
                                                .permitAll())
                                .exceptionHandling(ex -> ex
                                                .accessDeniedHandler(accessDeniedHandler()));

                // Añadir filtro JWT antes del filtro de autenticación de usuario
                http.addFilterBefore(jwtFiltro, UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }

        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
                return authConfig.getAuthenticationManager();
        }

        @Bean
        public AccessDeniedHandler accessDeniedHandler() {
                return (request, response, accessDeniedException) -> {
                        response.sendRedirect(request.getContextPath() + "/?error=access_denied");
                };
        }
}
