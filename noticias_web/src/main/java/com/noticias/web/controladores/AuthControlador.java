package com.noticias.web.controladores;

import com.noticias.web.dtos.*;
import com.noticias.web.servicios.AuthServicio;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

    // Endpoint de depuración para verificar el estado de la sesión y el rol
    @GetMapping("/debug/me")
    @ResponseBody
    public java.util.Map<String, Object> debugSession(HttpSession session) {
        UsuarioDTO usuario = (UsuarioDTO) session.getAttribute("usuario");
        java.util.Map<String, Object> info = new java.util.HashMap<>();
        if (usuario != null) {
            info.put("id", usuario.getId());
            info.put("email", usuario.getEmail());
            info.put("rolId", usuario.getRolId());
            info.put("rolNombre", usuario.getRolNombre()); // This is what Thymeleaf checks
            info.put("permissions", "Simulated Check: " +
                    ("ADMIN".equalsIgnoreCase(usuario.getRolNombre())
                            || "OWNER".equalsIgnoreCase(usuario.getRolNombre())
                            || "TRABAJADOR".equalsIgnoreCase(usuario.getRolNombre())));
        } else {
            info.put("status", "No user in session");
        }
        return info;
    }

    // ... (rest of methods)

    @PostMapping("/perfil/imagen/eliminar")
    public String eliminarAvatar(HttpSession session, RedirectAttributes redirectAttributes) {
        UsuarioDTO dto = (UsuarioDTO) session.getAttribute("usuario");
        if (dto == null) {
            return "redirect:/auth/login";
        }

        boolean ok = authServicio.eliminarAvatar(dto.getId());

        if (ok) {
            dto.setImagenUrl(null); // Clear session
            session.setAttribute("usuario", dto);
            redirectAttributes.addFlashAttribute("mensaje", "Foto de perfil eliminada.");
        } else {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar la foto.");
        }
        return "redirect:/perfil";
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
            // Fetch full user to get secretKey2FA and avoid Type Mismatch
            UsuarioDTO usuario = authServicio.buscarUsuarioPorEmail(email);
            // Update token in the fetched DTO just in case, though autenticar updates it in
            // DB
            usuario.setTokenSession(res.getToken());

            // 2FA Check for ADMIN, OWNER, or TRABAJADOR
            boolean isPrivileged = "ADMIN".equalsIgnoreCase(usuario.getRolNombre())
                    || "OWNER".equalsIgnoreCase(usuario.getRolNombre())
                    || "TRABAJADOR".equalsIgnoreCase(usuario.getRolNombre());

            if (isPrivileged) {
                // Store temp session for 2FA verification if enabled
                if (usuario.getSecretKey2FA() != null && !usuario.getSecretKey2FA().isEmpty()) {
                    session.setAttribute("temp_2fa_user", usuario);
                    session.setAttribute("temp_2fa_token", res.getToken());
                    return "redirect:/auth/2fa/verify";
                }
                // If 2FA not enabled, proceed to normal login (Optional 2FA)
            }

            // Normal Login Flow (Non-Privileged)
            completeLogin(session, usuario, res.getToken());

            if (redirect != null && !redirect.isEmpty()) {
                return "redirect:" + redirect;
            }
            return "redirect:/"; // Default redirect
        } catch (Exception ex) {
            model.addAttribute("error", ex.getMessage());
            if (redirect != null) {
                model.addAttribute("redirect", redirect);
            }
            return "vistas/Login";
        }
    }

    private void completeLogin(HttpSession session, UsuarioDTO usuario, String token) {
        session.setAttribute("token", token);
        session.setAttribute("usuario", usuario);
        // Clean temp 2fa
        session.removeAttribute("temp_2fa_user");
        session.removeAttribute("temp_2fa_token");
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

    @GetMapping("/2fa/setup")
    public String setup2faPage(HttpSession session, Model model) {
        UsuarioDTO user = (UsuarioDTO) session.getAttribute("temp_2fa_user");
        if (user == null) {
            // Try to get fully logged in user (Re-configuration from Profile)
            user = (UsuarioDTO) session.getAttribute("usuario");
        }

        if (user == null) {
            return "redirect:/auth/login";
        }

        com.warrenstrange.googleauth.GoogleAuthenticator gAuth = new com.warrenstrange.googleauth.GoogleAuthenticator();
        final com.warrenstrange.googleauth.GoogleAuthenticatorKey key = gAuth.createCredentials();
        String secret = key.getKey();

        session.setAttribute("temp_2fa_secret", secret);

        // Generate QR URL (Using simple chart api or custom)
        // Format: otpauth://totp/NoticiasApp:userEmail?secret=SECRET&issuer=NoticiasApp
        String otpAuthUrl = "otpauth://totp/NoticiasApp:" + user.getEmail() + "?secret=" + secret
                + "&issuer=NoticiasApp";
        // QR Generator is separate, but we can use quickchart.io for simplicity
        // client-side or generate here
        // Or using Google Chart API (Deprecated but works) or a library.
        // For simplicity, let's use a public QR API.
        String qrUrl = "https://api.qrserver.com/v1/create-qr-code/?size=200x200&data="
                + java.net.URLEncoder.encode(otpAuthUrl, java.nio.charset.StandardCharsets.UTF_8);

        model.addAttribute("qrUrl", qrUrl);
        return "vistas/2fa-setup";
    }

    @PostMapping("/2fa/setup")
    public String setup2faPost(@RequestParam("code") int code, HttpSession session, Model model) {
        UsuarioDTO user = (UsuarioDTO) session.getAttribute("temp_2fa_user");
        boolean isTempUser = true;

        if (user == null) {
            user = (UsuarioDTO) session.getAttribute("usuario");
            isTempUser = false;
        }

        String secret = (String) session.getAttribute("temp_2fa_secret");
        String token = (String) session.getAttribute("temp_2fa_token");

        if (user == null || secret == null) {
            return "redirect:/auth/login";
        }

        com.warrenstrange.googleauth.GoogleAuthenticator gAuth = new com.warrenstrange.googleauth.GoogleAuthenticator();
        if (gAuth.authorize(secret, code)) {
            // Save secret to DB
            UsuarioDTO updateDto = new UsuarioDTO();
            updateDto.setSecretKey2FA(secret);

            authServicio.activar2FA(user.getId(), secret);

            if (isTempUser) {
                // Login user
                completeLogin(session, user, token);
                return "redirect:/";
            } else {
                // Already logged in, just update session and redirect to profile
                user.setSecretKey2FA(secret);
                session.setAttribute("usuario", user);
                return "redirect:/perfil";
            }
        } else {
            model.addAttribute("error", "Código incorrecto");
            // Re-render setup page with NEW secret? Or same? Better same.
            // We need to re-generate QR url though.
            String otpAuthUrl = "otpauth://totp/NoticiasApp:" + user.getEmail() + "?secret=" + secret
                    + "&issuer=NoticiasApp";
            String qrUrl = "https://api.qrserver.com/v1/create-qr-code/?size=200x200&data="
                    + java.net.URLEncoder.encode(otpAuthUrl, java.nio.charset.StandardCharsets.UTF_8);
            model.addAttribute("qrUrl", qrUrl);
            return "vistas/2fa-setup";
        }
    }

    @GetMapping("/2fa/verify")
    public String verify2faPage(HttpSession session) {
        if (session.getAttribute("temp_2fa_user") == null) {
            return "redirect:/auth/login";
        }
        return "vistas/2fa-verify";
    }

    @PostMapping("/2fa/verify")
    public String verify2faPost(@RequestParam("code") int code, HttpSession session, Model model) {
        UsuarioDTO user = (UsuarioDTO) session.getAttribute("temp_2fa_user");
        String token = (String) session.getAttribute("temp_2fa_token");

        if (user == null) {
            return "redirect:/auth/login";
        }

        // Rate Limiting
        Integer attempts = (Integer) session.getAttribute("2fa_attempts");
        if (attempts == null)
            attempts = 0;

        if (attempts >= 3) {
            session.removeAttribute("temp_2fa_user");
            session.removeAttribute("temp_2fa_token");
            session.removeAttribute("2fa_attempts");
            model.addAttribute("error",
                    "Has excedido el límite de intentos (3). Por seguridad, inicia sesión de nuevo.");
            return "vistas/Login";
        }

        com.warrenstrange.googleauth.GoogleAuthenticator gAuth = new com.warrenstrange.googleauth.GoogleAuthenticator();
        if (gAuth.authorize(user.getSecretKey2FA(), code)) {
            session.removeAttribute("2fa_attempts");
            completeLogin(session, user, token);
            return "redirect:/";
        } else {
            attempts++;
            session.setAttribute("2fa_attempts", attempts);
            model.addAttribute("error", "Código incorrecto. Intentos restantes: " + (3 - attempts));
            return "vistas/2fa-verify";
        }
    }

    @PostMapping("/2fa/disable")
    public String disable2fa(HttpSession session, RedirectAttributes redirectAttributes) {
        UsuarioDTO usuario = (UsuarioDTO) session.getAttribute("usuario");
        if (usuario != null) {
            authServicio.desactivar2FA(usuario.getId());
            usuario.setSecretKey2FA(null);
            session.setAttribute("usuario", usuario);
            redirectAttributes.addFlashAttribute("mensaje", "Autenticación en dos pasos desactivada correctamente.");
        }
        return "redirect:/perfil";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        UsuarioDTO usuario = (UsuarioDTO) session.getAttribute("usuario");
        if (usuario != null) {
            try {
                authServicio.cerrarSesion(usuario.getId());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        session.invalidate();
        return "redirect:/";
    }

    @PostMapping("/perfil/actualizar")
    public String actualizarPerfil(@RequestParam String nombreCompleto, @RequestParam String email,
            HttpSession session, RedirectAttributes redirectAttributes) {
        UsuarioDTO usuario = (UsuarioDTO) session.getAttribute("usuario");
        if (usuario != null) {
            try {
                String result = authServicio.actualizarPerfil(usuario.getId(), nombreCompleto, email,
                        usuario.getEmail());

                // Always update name in session
                usuario.setNombreCompleto(nombreCompleto);

                if ("VERIFY".equals(result)) {
                    redirectAttributes.addFlashAttribute("mensaje",
                            "Perfil actualizado. Se ha enviado un correo a tu nuevo email para confirmarlo.");
                    // Do NOT update email in session yet
                } else {
                    redirectAttributes.addFlashAttribute("mensaje", "Perfil actualizado correctamente.");
                    usuario.setEmail(email);
                    session.setAttribute("usuario", usuario);
                }
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("error", "Error actualizando perfil: " + e.getMessage());
            }
        }
        return "redirect:/perfil";
    }

    @PostMapping("/perfil/imagen")
    public String subirAvatar(@RequestParam("file") org.springframework.web.multipart.MultipartFile file,
            HttpSession session) {
        UsuarioDTO usuario = (UsuarioDTO) session.getAttribute("usuario");
        if (usuario != null && !file.isEmpty()) {
            String url = authServicio.subirAvatar(usuario.getId(), file);
            if (url != null) {
                usuario.setImagenUrl(url);
                session.setAttribute("usuario", usuario);
            }
        }
        return "redirect:/perfil";
    }

}
