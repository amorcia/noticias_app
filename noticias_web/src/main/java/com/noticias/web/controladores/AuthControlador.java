package com.noticias.web.controladores;

import com.noticias.web.dtos.*;
import com.noticias.web.servicios.AuthServicio;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador de autenticación para News App.
 * Maneja login, registro, recuperación de contraseña y confirmación de email.
 */
@Controller
@RequestMapping("/auth")
public class AuthControlador {

    private final AuthServicio authServicio;

    public AuthControlador(AuthServicio authServicio) {
        this.authServicio = authServicio;
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String redirect, HttpSession session, Model model) {
        if (session.getAttribute("token") != null) {
            return "redirect:/";
        }
        if (redirect != null) {
            model.addAttribute("redirect", redirect);
        }
        return "vistas/Login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email,
            @RequestParam String password,
            @RequestParam(required = false) String redirect,
            HttpSession session,
            Model model) {
        try {
            LoginRespuestaDTO res = authServicio.autenticar(email, password);

            // Convertir UsuarioRespuestaDTO a UsuarioDTO para la sesión
            UsuarioDTO usuarioSession = new UsuarioDTO();
            usuarioSession.setId(res.getUsuario().getId());
            usuarioSession.setNombreCompleto(res.getUsuario().getNombre());
            usuarioSession.setEmail(res.getUsuario().getEmail());
            usuarioSession.setRolNombre(res.getUsuario().getRol());
            usuarioSession.setActivo(res.getUsuario().getActivo());
            usuarioSession.setTokenSession(res.getToken());

            session.setAttribute("token", res.getToken());
            session.setAttribute("usuario", usuarioSession);

            // Smart Redirect
            if (redirect != null && !redirect.isEmpty()) {
                return "redirect:" + redirect;
            }

            return "redirect:/";
        } catch (Exception ex) {
            model.addAttribute("error", ex.getMessage());
            if (redirect != null) {
                model.addAttribute("redirect", redirect);
            }
            return "vistas/Login";
        }
    }

    @GetMapping("/registro")
    public String registroPage(HttpSession session) {
        if (session.getAttribute("token") != null) {
            return "redirect:/";
        }
        return "vistas/Registro";
    }

    @PostMapping("/registro")
    public String registro(@RequestParam String nombreCompleto,
            @RequestParam String email,
            @RequestParam(required = false) String movil,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            Model model) {
        try {
            if (!password.equals(confirmPassword)) {
                model.addAttribute("error", "Las contraseñas no coinciden");
                return "vistas/Registro";
            }

            authServicio.registrarUsuario(nombreCompleto, email, movil, password);
            model.addAttribute("mensaje", "Registro exitoso. Por favor revisa tu email para confirmar tu cuenta.");
            return "vistas/Login";
        } catch (Exception ex) {
            model.addAttribute("error", ex.getMessage());
            return "vistas/Registro";
        }
    }

    @GetMapping("/confirmar")
    public String confirmarEmail(@RequestParam String token, Model model) {
        try {
            boolean confirmado = authServicio.confirmarEmail(token);
            if (confirmado) {
                model.addAttribute("mensaje", "Email confirmado exitosamente. Ya puedes iniciar sesión.");
                return "vistas/Login";
            } else {
                model.addAttribute("error", "Token inválido o expirado.");
                return "vistas/Login";
            }
        } catch (Exception ex) {
            model.addAttribute("error", "Error al confirmar email: " + ex.getMessage());
            return "vistas/Login";
        }
    }

    @GetMapping("/olvidar")
    public String olvidarPage() {
        return "vistas/RecuperarEmail";
    }

    @PostMapping("/olvidar")
    public String olvidar(@RequestParam String email, Model model) {
        try {
            authServicio.generarYEnviarCodigoRecuperacion(email);
            return "redirect:/auth/verificar-codigo?email=" + email;
        } catch (Exception ex) {
            model.addAttribute("error", ex.getMessage());
            return "vistas/RecuperarEmail";
        }
    }

    @GetMapping("/verificar-codigo")
    public String verificarCodigoPage(@RequestParam String email, Model model) {
        model.addAttribute("email", email);
        return "vistas/RecuperarCodigo";
    }

    @PostMapping("/verificar-codigo")
    public String verificarCodigo(@RequestParam String email, @RequestParam String codigo, Model model) {
        try {
            // Verificar código contra el backend
            // NOTA: Necesitamos añadir este metodo a AuthServicio y API
            // Por ahora, asumimos que AuthServicio lo tiene (lo añadimos en el replace
            // anterior)
            boolean valido = authServicio.verificarCodigo(email, codigo);
            if (valido) {
                // Redirigir a reset con el código como token
                return "redirect:/auth/reset-password?token=" + codigo;
            } else {
                model.addAttribute("error", "Código incorrecto o expirado");
                model.addAttribute("email", email);
                return "vistas/RecuperarCodigo";
            }
        } catch (Exception ex) {
            model.addAttribute("error", "Error al verificar código: " + ex.getMessage());
            model.addAttribute("email", email);
            return "vistas/RecuperarCodigo";
        }
    }

    @GetMapping("/reset-password")
    public String resetPasswordPage(@RequestParam String token, Model model) {
        model.addAttribute("token", token); // El token es el código
        return "vistas/RecuperarReset";
    }

    @PostMapping("/reset-password")
    public String processResetPassword(@RequestParam String token,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            Model model) {
        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("error", "Las contraseñas no coinciden");
            model.addAttribute("token", token);
            return "vistas/RecuperarReset";
        }

        try {
            boolean restablecido = authServicio.restablecerPassword(token, newPassword);
            if (restablecido) {
                model.addAttribute("mensaje", "Contraseña restablecida exitosamente.");
                return "vistas/Login";
            } else {
                model.addAttribute("error", "Código inválido o expirado.");
                model.addAttribute("token", token);
                return "vistas/RecuperarReset";
            }
        } catch (Exception ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("token", token);
            return "vistas/RecuperarReset";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        UsuarioDTO usuario = (UsuarioDTO) session.getAttribute("usuario");
        if (usuario != null) {
            try {
                // Invalidar token en DB
                // Necesitamos authServicio para esto, pero estamos en Controlador.
                // Deberia haber un metodo en servicio.
                // Como no puedo inyectar servicio aqui facil sin cambiar constructor (ya esta
                // inyectado),
                // llamare a un nuevo metodo en authServicio.
                authServicio.cerrarSesion(usuario.getId());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        session.invalidate();
        return "redirect:/";
    }
}
