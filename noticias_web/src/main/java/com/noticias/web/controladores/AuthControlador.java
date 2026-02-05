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

    /**
     * @author amorcia
     *         METODO - Elimina la foto de perfil del usuario actual
     * @param session            Sesión HTTP actual
     * @param redirectAttributes Atributos para redirección (mensajes flash)
     * @return Redirección a perfil o login
     */
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

    /**
     * @author amorcia
     *         METODO - Muestra la página de inicio de sesión
     * @param redirect URL opcional para redireccionar después del login
     * @param session  Sesión HTTP
     * @param model    Modelo para la vista
     * @return Nombre de la vista de login o redirección si ya está logueado
     */
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

    /**
     * @author amorcia
     *         METODO - Procesa el inicio de sesión del usuario
     * @param email    Email del usuario
     * @param password Contraseña
     * @param redirect URL de redirección opcional
     * @param session  Sesión HTTP
     * @param model    Modelo para la vista
     * @return Vista o redirección según el resultado
     */
    @PostMapping("/login")
    public String login(@RequestParam String email,
            @RequestParam String password,
            @RequestParam(required = false) String redirect,
            HttpSession session,
            Model model) {
        try {
            LoginRespuestaDTO res = authServicio.autenticar(email, password);
            UsuarioDTO usuario = authServicio.buscarUsuarioPorEmail(email);
            usuario.setTokenSession(res.getToken());

            // 2FA Check for privileged roles
            boolean isPrivileged = "ADMIN".equalsIgnoreCase(usuario.getRolNombre())
                    || "OWNER".equalsIgnoreCase(usuario.getRolNombre())
                    || "TRABAJADOR".equalsIgnoreCase(usuario.getRolNombre());

            if (isPrivileged && usuario.getSecretKey2FA() != null && !usuario.getSecretKey2FA().isEmpty()) {
                session.setAttribute("temp_2fa_user", usuario);
                session.setAttribute("temp_2fa_token", res.getToken());
                return "redirect:/auth/2fa/verify";
            }

            completeLogin(session, usuario, res.getToken());

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

    /**
     * @author amorcia
     *         METODO - Helper para completar el login y establecer sesión
     */
    private void completeLogin(HttpSession session, UsuarioDTO usuario, String token) {
        session.setAttribute("token", token);
        session.setAttribute("usuario", usuario);
        session.removeAttribute("temp_2fa_user");
        session.removeAttribute("temp_2fa_token");
    }

    /**
     * @author amorcia
     *         METODO - Muestra la página de registro
     * @param session Sesión HTTP
     * @return Vista de registro o redirección
     */
    @GetMapping("/registro")
    public String registroPage(HttpSession session) {
        if (session.getAttribute("token") != null) {
            return "redirect:/";
        }
        return "vistas/Registro";
    }

    /**
     * @author amorcia
     *         METODO - Helper para validar datos de registro
     * @return Mensaje de error o null si es válido
     */
    private String validarDatosRegistro(String password, String confirmPassword, String movil) {
        if (!password.equals(confirmPassword))
            return "Las contraseñas no coinciden";
        if (movil == null || movil.trim().isEmpty())
            return "El número de móvil es obligatorio";
        if (!movil.matches("[0-9]{9}"))
            return "El móvil debe tener exactamente 9 dígitos";
        if (password.length() < 6)
            return "La contraseña debe tener al menos 6 caracteres";
        if (!password.matches(".*[A-Z].*"))
            return "La contraseña debe contener al menos una letra mayúscula";
        if (!password.matches(".*[a-z].*"))
            return "La contraseña debe contener al menos una letra minúscula";
        if (!password.matches(".*[0-9].*"))
            return "La contraseña debe contener al menos un número";
        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*"))
            return "La contraseña debe contener al menos un carácter especial (!@#$%^&*)";
        return null;
    }

    /**
     * @author amorcia
     *         METODO - Procesa el registro de un nuevo usuario
     * @param nombreCompleto  Nombre completo
     * @param email           Email
     * @param movil           Teléfono móvil
     * @param password        Contraseña
     * @param confirmPassword Confirmación de contraseña
     * @param model           Modelo para la vista
     * @return Vista de éxito (login) o registro con error
     */
    @PostMapping("/registro")
    public String registro(@RequestParam String nombreCompleto,
            @RequestParam String email,
            @RequestParam(required = false) String movil,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            Model model) {
        try {
            String errorValidacion = validarDatosRegistro(password, confirmPassword, movil);
            if (errorValidacion != null) {
                model.addAttribute("error", errorValidacion);
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

    /**
     * @author amorcia
     *         METODO - Confirma el email del usuario mediante token (código de 6
     *         dígitos)
     * @param token Token/Código de verificación
     * @param model Modelo para la vista
     * @return Vista de login con mensaje de éxito o error
     */
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

    /**
     * @author amorcia
     *         METODO - Muestra el formulario para recuperar contraseña
     * @return Vista recuperar email
     */
    @GetMapping("/olvidar")
    public String olvidarPage() {
        return "vistas/RecuperarEmail";
    }

    /**
     * @author amorcia
     *         METODO - Procesa la solicitud de recuperación de contraseña
     * @param email Email del usuario
     * @param model Modelo
     * @return Redirección a verificación de código o error
     */
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

    /**
     * @author amorcia
     *         METODO - Muestra página para verificar código de recuperación
     * @param email Email del usuario
     * @param model Modelo
     */
    @GetMapping("/verificar-codigo")
    public String verificarCodigoPage(@RequestParam String email, Model model) {
        model.addAttribute("email", email);
        return "vistas/RecuperarCodigo";
    }

    /**
     * @author amorcia
     *         METODO - Verifica el código de recuperación ingresado
     * @param email  Email
     * @param codigo Código de 6 dígitos
     * @param model  Modelo
     * @return Redirección a reset password si es válido
     */
    @PostMapping("/verificar-codigo")
    public String verificarCodigo(@RequestParam String email, @RequestParam String codigo, Model model) {
        try {
            boolean valido = authServicio.verificarCodigo(email, codigo);
            if (valido) {
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

    /**
     * @author amorcia
     *         METODO - Muestra formulario para establecer nueva contraseña
     * @param token Código (usado como token)
     * @param model Modelo
     */
    @GetMapping("/reset-password")
    public String resetPasswordPage(@RequestParam String token, Model model) {
        model.addAttribute("token", token);
        return "vistas/RecuperarReset";
    }

    /**
     * @author amorcia
     *         METODO - Procesa el cambio de contraseña
     * @param token           Código/Token
     * @param newPassword     Nueva contraseña
     * @param confirmPassword Confirmación
     * @param model           Modelo
     * @return Redirección a login si éxito
     */
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

    /**
     * @author amorcia
     *         METODO - Configuración de 2FA (GET)
     */
    @GetMapping("/2fa/setup")
    public String setup2faPage(HttpSession session, Model model) {
        UsuarioDTO user = (UsuarioDTO) session.getAttribute("temp_2fa_user");
        if (user == null) {
            user = (UsuarioDTO) session.getAttribute("usuario");
        }

        if (user == null) {
            return "redirect:/auth/login";
        }

        com.warrenstrange.googleauth.GoogleAuthenticator gAuth = new com.warrenstrange.googleauth.GoogleAuthenticator();
        final com.warrenstrange.googleauth.GoogleAuthenticatorKey key = gAuth.createCredentials();
        String secret = key.getKey();

        session.setAttribute("temp_2fa_secret", secret);

        String otpAuthUrl = "otpauth://totp/NoticiasApp:" + user.getEmail() + "?secret=" + secret
                + "&issuer=NoticiasApp";
        String qrUrl = "https://api.qrserver.com/v1/create-qr-code/?size=200x200&data="
                + java.net.URLEncoder.encode(otpAuthUrl, java.nio.charset.StandardCharsets.UTF_8);

        model.addAttribute("qrUrl", qrUrl);
        return "vistas/2fa-setup";
    }

    /**
     * @author amorcia
     *         METODO - Procesa configuración de 2FA (POST) verificando código
     */
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
            authServicio.activar2FA(user.getId(), secret);

            if (isTempUser) {
                completeLogin(session, user, token);
                return "redirect:/";
            } else {
                user.setSecretKey2FA(secret);
                session.setAttribute("usuario", user);
                return "redirect:/perfil";
            }
        } else {
            model.addAttribute("error", "Código incorrecto");
            String otpAuthUrl = "otpauth://totp/NoticiasApp:" + user.getEmail() + "?secret=" + secret
                    + "&issuer=NoticiasApp";
            String qrUrl = "https://api.qrserver.com/v1/create-qr-code/?size=200x200&data="
                    + java.net.URLEncoder.encode(otpAuthUrl, java.nio.charset.StandardCharsets.UTF_8);
            model.addAttribute("qrUrl", qrUrl);
            return "vistas/2fa-setup";
        }
    }

    /**
     * @author amorcia
     *         METODO - Página de verificación 2FA (Login)
     */
    @GetMapping("/2fa/verify")
    public String verify2faPage(HttpSession session) {
        if (session.getAttribute("temp_2fa_user") == null) {
            return "redirect:/auth/login";
        }
        return "vistas/2fa-verify";
    }

    /**
     * @author amorcia
     *         METODO - Procesa verificación 2FA (Login)
     */
    @PostMapping("/2fa/verify")
    public String verify2faPost(@RequestParam("code") int code, HttpSession session, Model model) {
        UsuarioDTO user = (UsuarioDTO) session.getAttribute("temp_2fa_user");
        String token = (String) session.getAttribute("temp_2fa_token");

        if (user == null) {
            return "redirect:/auth/login";
        }

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

    /**
     * @author amorcia
     *         METODO - Desactiva 2FA
     */
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

    /**
     * @author amorcia
     *         METODO - Cierra la sesión
     */
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

    /**
     * @author amorcia
     *         METODO - Actualiza perfil de usuario
     */
    @PostMapping("/perfil/actualizar")
    public String actualizarPerfil(@RequestParam String nombreCompleto, @RequestParam String email,
            HttpSession session, RedirectAttributes redirectAttributes) {
        UsuarioDTO usuario = (UsuarioDTO) session.getAttribute("usuario");
        if (usuario != null) {
            try {
                String result = authServicio.actualizarPerfil(usuario.getId(), nombreCompleto, email,
                        usuario.getEmail());
                usuario.setNombreCompleto(nombreCompleto);

                if ("VERIFY".equals(result)) {
                    redirectAttributes.addFlashAttribute("mensaje",
                            "Perfil actualizado. Se ha enviado un correo a tu nuevo email para confirmarlo.");
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

    /**
     * @author amorcia
     *         METODO - Sube imagen de perfil
     */
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
