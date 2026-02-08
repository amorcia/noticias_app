package com.noticias.web.controladores;

import com.noticias.web.dtos.CategoriaDTO;
import com.noticias.web.dtos.UsuarioDTO;
import com.noticias.web.servicios.ApiNoticiasCliente;
import com.noticias.web.seguridad.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

@ControllerAdvice
public class GlobalDataAdvice {

    @Autowired
    private ApiNoticiasCliente apiCliente;

    @Autowired
    private JwtUtil jwtUtil;

    @ModelAttribute
    public void addGlobalAttributes(Model model, HttpServletRequest request) {
        try {
            // Cargar categorías
            try {
                if (apiCliente != null) {
                    List<CategoriaDTO> categorias = apiCliente.listarCategorias();
                    if (categorias != null && !categorias.isEmpty()) {
                        model.addAttribute("categorias", categorias);
                        System.out.println("✅ Categorías cargadas: " + categorias.size());
                    } else {
                        model.addAttribute("categorias", List.of());
                        System.out.println("⚠️ Lista de categorías vacía");
                    }
                } else {
                    model.addAttribute("categorias", List.of());
                    System.out.println("❌ apiCliente es null");
                }
            } catch (Exception e) {
                model.addAttribute("categorias", List.of());
                System.out.println("❌ Error cargando categorías: " + e.getMessage());
                e.printStackTrace();
            }

            // Cargar usuario
            boolean isLoggedIn = false;
            UsuarioDTO currentUser = null;

            try {
                HttpSession session = request.getSession(false);
                if (session != null) {
                    String token = (String) session.getAttribute("token");
                    UsuarioDTO usuarioSession = (UsuarioDTO) session.getAttribute("usuario");

                    System.out.println("🔍 GlobalDataAdvice - Session ID: " + session.getId());
                    System.out.println("🔍 GlobalDataAdvice - Token en session: " + (token != null ? "SI" : "NO"));
                    System.out.println("🔍 GlobalDataAdvice - Usuario en session: "
                            + (usuarioSession != null ? usuarioSession.getEmail() : "NO"));

                    if (token != null && jwtUtil != null) {
                        if (jwtUtil.validarToken(token)) {
                            isLoggedIn = true;
                            if (usuarioSession != null) {
                                currentUser = usuarioSession;
                            } else {
                                try {
                                    String email = jwtUtil.extraerEmail(token);
                                    if (apiCliente != null) {
                                        currentUser = apiCliente.buscarUsuarioPorEmail(email);
                                        session.setAttribute("usuario", currentUser);
                                    }
                                } catch (Exception e) {
                                    System.out.println("❌ Error recuperando usuario por token: " + e.getMessage());
                                    isLoggedIn = false;
                                }
                            }
                            // Asegurar que el modelo tenga el usuario de la sesión para evitar
                            // inconsistencias
                            model.addAttribute("usuario", currentUser);
                        } else {
                            System.out.println("⚠️ Token inválido o expirado");
                        }
                    }
                } else {
                    System.out.println("⚠️ No hay sesión activa");
                }
            } catch (Exception e) {
                System.out.println("❌ Error verificando sesión: " + e.getMessage());
                isLoggedIn = false;
            }

            model.addAttribute("isLoggedIn", isLoggedIn);
            model.addAttribute("currentUser", currentUser);

        } catch (Exception e) {
            System.out.println("❌ Error CRÍTICO en GlobalDataAdvice: " + e.getMessage());
            e.printStackTrace();
            // Fallback total
            model.addAttribute("categorias", List.of());
            model.addAttribute("isLoggedIn", false);
            model.addAttribute("currentUser", null);
        }
    }
}
