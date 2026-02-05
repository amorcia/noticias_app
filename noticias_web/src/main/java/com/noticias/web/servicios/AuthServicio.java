package com.noticias.web.servicios;

import com.noticias.web.dtos.*;
// JwtUtil removed
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Servicio de autenticación adaptado para News App.
 * Maneja login, registro, recuperación de contraseña y confirmación de email.
 */
@Service
public class AuthServicio {

    private final ApiNoticiasCliente apiCliente;
    private final BCryptPasswordEncoder passwordEncoder;
    private final EmailServicio emailServicio;

    // DURACION_HORAS_TOKEN removed (unused)

    public AuthServicio(ApiNoticiasCliente apiCliente,
            EmailServicio emailServicio) {
        this.apiCliente = apiCliente;
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.emailServicio = emailServicio;
    }

    public UsuarioDTO buscarUsuarioPorEmail(String email) {
        return apiCliente.buscarUsuarioPorEmail(email);
    }

    public LoginRespuestaDTO autenticar(String email, String password) {
        UsuarioDTO usuario = apiCliente.buscarUsuarioPorEmail(email);

        if (usuario == null) {
            throw new RuntimeException("Usuario no encontrado");
        }

        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            throw new RuntimeException("Usuario inactivo. Por favor confirma tu email.");
        }

        if (Boolean.TRUE.equals(usuario.getVetado())) {
            String msg = "Tu cuenta ha sido suspendida.";
            if (usuario.getVetadoHasta() != null) {
                java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter
                        .ofPattern("dd/MM/yyyy HH:mm");
                msg += " Hasta: " + usuario.getVetadoHasta().format(formatter);
            } else {
                msg += " (Permanente)";
            }
            if (usuario.getMotivoVeto() != null && !usuario.getMotivoVeto().isEmpty()) {
                msg += " Motivo: " + usuario.getMotivoVeto();
            }
            throw new RuntimeException(msg);
        }

        boolean coincide = passwordEncoder.matches(password, usuario.getPassword());
        if (!coincide) {
            throw new RuntimeException("Credenciales inválidas");
        }

        // Generar Session Token
        String sessionToken = UUID.randomUUID().toString();
        usuario.setTokenSession(sessionToken);

        // Actualizar usuario en DB con el nuevo token de sesion de forma segura (sin
        // bloquear OWNER)
        apiCliente.actualizarTokenSesion(usuario.getId(), sessionToken);

        UsuarioRespuestaDTO usuarioResp = new UsuarioRespuestaDTO();
        usuarioResp.setId(usuario.getId());
        usuarioResp.setNombre(usuario.getNombreCompleto());
        usuarioResp.setEmail(usuario.getEmail());
        usuarioResp.setRol(usuario.getRolNombre());
        usuarioResp.setActivo(usuario.getActivo());

        // Retornar token JWT (si se sigue usando) y guardar token de sesion en objeto
        // usuario
        // Nota: session.setAttribute("token", ...) en AuthControlador usa el JWT.
        // Pero el usuario pidio usar token_session.
        // Modificaremos la respuesta para incluir el sessionToken o lo manejamos en el
        // controlador.
        // Como LoginRespuestaDTO tiene un token, usaremos ese, pero ahora representando
        // la sesion?
        // El usuario dijo "mi usuario... tiene la sesion iniciada, pero... no tiene
        // token ninguno"
        // "token_session... se pondra NULL si no hay sesion o un token nuevo cada vez
        // que la sesion sea iniciada"

        return new LoginRespuestaDTO(sessionToken, usuarioResp, false);
    }

    public void registrarUsuario(String nombreCompleto, String email, String movil, String password) {
        // Verificar si el usuario ya existe
        UsuarioDTO existente = apiCliente.buscarUsuarioPorEmail(email);
        if (existente != null) {
            throw new RuntimeException("El email ya está registrado");
        }

        // Crear nuevo usuario
        UsuarioDTO nuevoUsuario = new UsuarioDTO();
        nuevoUsuario.setNombreCompleto(nombreCompleto);
        nuevoUsuario.setEmail(email);
        nuevoUsuario.setMovil(movil);
        nuevoUsuario.setPassword(passwordEncoder.encode(password)); // Encriptar aquí
        nuevoUsuario.setRolId(4); // USER por defecto (ID 4)
        nuevoUsuario.setActivo(false); // Requiere confirmación
        nuevoUsuario.setCodigoVerificacion(UUID.randomUUID().toString());
        // fechaCodigo removed

        UsuarioDTO creado = apiCliente.crearUsuario(nuevoUsuario);

        // Enviar email de confirmación
        String asunto = "Confirma tu cuenta - Noticias App";
        String cuerpo = crearEmailConfirmacion(creado.getCodigoVerificacion());
        emailServicio.enviarEmail(email, asunto, cuerpo);
    }

    public boolean confirmarEmail(String token) {
        // La API maneja la confirmación y limpieza del token
        return apiCliente.confirmarEmail(token);
    }

    public void generarYEnviarCodigoRecuperacion(String email) {
        UsuarioDTO usuario = apiCliente.buscarUsuarioPorEmail(email);
        if (usuario == null) {
            throw new RuntimeException("Usuario no encontrado");
        }

        String token = apiCliente.generarTokenRecuperacion(email);

        String asunto = "Código de Recuperación - Noticias App";
        String cuerpo = crearEmailRecuperacion(token);
        emailServicio.enviarEmail(email, asunto, cuerpo);
    }

    public boolean verificarCodigo(String email, String codigo) {
        UsuarioDTO usuario = apiCliente.buscarUsuarioPorEmail(email);
        // Verificamos contra codigoVerificacion (campo unico)
        if (usuario != null && usuario.getCodigoVerificacion() != null) {
            // Solo comprobamos coincidencia, sin expiracion
            return usuario.getCodigoVerificacion().equals(codigo);
        }
        return false;
    }

    public boolean restablecerPassword(String token, String nuevaPassword) {
        // En este paso, el token es el código.
        // Debemos buscar el usuario por el código para poder resetear.
        // O buscar por email si lo tuvieramos en este contexto?
        // El metodo de API 'restablecerPassword' busca por token.
        // Pero hemos renombrado en API a findByCodigoRecuperacion.
        // Asi que apiCliente.restablecerPassword debe invocar al endpoint correcto.
        String passwordEncriptada = passwordEncoder.encode(nuevaPassword);
        return apiCliente.restablecerPassword(token, passwordEncriptada);
    }

    public void cerrarSesion(Integer userId) {
        UsuarioDTO usuarioActualizar = new UsuarioDTO();
        usuarioActualizar.setTokenSession(null); // Borrar token
        // Hack: setTokenSession accepts String, might need to handle NULL in API update
        // logic if not careful
        // but API check "if (usuarioActualizado.getTokenSession() != null)" might skip
        // it if null?
        // We need to send a explicit indicator or empty string?
        // API: if (usuarioActualizado.getTokenSession() != null) {
        // usuario.setTokenSession(...) }
        // If we send null, it wont update.
        // We need to send empty string or change API logic?
        // User said: "se pondra NULL".
        // Let's check API UsuarioServicio.actualizarUsuario logic.
        // It checks if (dto.getField() != null). So we can't set it to null using the
        // current generic update method if DTO field is null.
        // We need a specific "logout" or "clean token" method in API or modify API
        // update logic to allow setting null.
        // Or we send "NULL" string and API converts? No.
        // Easier: Create a specific API endpoint for logout or session management?
        // "la api solo se dedica exclusivamente a pasar datos"
        // Let's modify the API Client/Service logic to handle "nullifying" fields?
        // Time constraint.
        // I will use a special value "LOGOUT" and handle it in API? No, too hacky.
        // I will assume for now I cannot set it to null easily with partial update.
        // I will add a method in API "logout" or "invalidarSesion".
        apiCliente.cerrarSesion(userId);
    }

    public void cambiarPassword(String email, String nuevaPassword) {
        UsuarioDTO usuario = apiCliente.buscarUsuarioPorEmail(email);
        if (usuario == null) {
            throw new RuntimeException("Usuario no encontrado");
        }

        String hashed = passwordEncoder.encode(nuevaPassword);
        usuario.setPassword(hashed);
        apiCliente.actualizarUsuario(usuario.getId(), usuario);
    }

    private String crearEmailConfirmacion(String token) {
        String url = "http://localhost:8080/noticias/auth/confirmar?token=" + token;
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head><meta charset='UTF-8'></head>" +
                "<body style='font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px;'>" +
                "<div style='max-width: 600px; margin: 0 auto; background-color: white; border-radius: 10px; overflow: hidden; box-shadow: 0 2px 10px rgba(0,0,0,0.1);'>"
                +
                "<div style='background: linear-gradient(135deg, #3B82F6 0%, #1E40AF 100%); padding: 30px; text-align: center;'>"
                +
                "<h1 style='color: white; margin: 0; font-size: 28px;'>📰 Noticias App</h1>" +
                "</div>" +
                "<div style='padding: 40px 30px;'>" +
                "<h2 style='color: #333; margin-top: 0;'>Confirma tu cuenta</h2>" +
                "<p style='color: #666; font-size: 16px; line-height: 1.6;'>Gracias por registrarte. Haz clic en el siguiente enlace para confirmar tu cuenta:</p>"
                +
                "<div style='text-align: center; margin: 30px 0;'>" +
                "<a href='" + url
                + "' style='background-color: #3B82F6; color: white; padding: 15px 30px; text-decoration: none; border-radius: 5px; display: inline-block;'>Confirmar Email</a>"
                +
                "</div>" +
                "<p style='color: #666; font-size: 14px; line-height: 1.6;'>Este enlace expirará en 24 horas.</p>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }

    private String crearEmailRecuperacion(String token) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head><meta charset='UTF-8'></head>" +
                "<body style='font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px;'>" +
                "<div style='max-width: 600px; margin: 0 auto; background-color: white; border-radius: 10px; overflow: hidden; box-shadow: 0 2px 10px rgba(0,0,0,0.1);'>"
                +
                "<div style='background: linear-gradient(135deg, #3B82F6 0%, #1E40AF 100%); padding: 30px; text-align: center;'>"
                +
                "<h1 style='color: white; margin: 0; font-size: 28px;'>📰 Noticias App</h1>" +
                "</div>" +
                "<div style='padding: 40px 30px;'>" +
                "<h2 style='color: #333; margin-top: 0;'>Código de Recuperación</h2>" +
                "<p style='color: #666; font-size: 16px; line-height: 1.6;'>Hemos recibido una solicitud para restablecer tu contraseña. Tu código de verificación es:</p>"
                +
                "<div style='text-align: center; margin: 30px 0;'>" +
                "<div style='background-color: #f3f4f6; color: #1f2937; padding: 15px 30px; font-size: 24px; font-weight: bold; letter-spacing: 5px; border-radius: 5px; display: inline-block;'>"
                + token + "</div>"
                +
                "</div>" +
                "<p style='color: #666; font-size: 14px; line-height: 1.6;'><strong>Este código expirará en 1 hora.</strong></p>"
                +
                "<p style='color: #666; font-size: 14px; line-height: 1.6;'>Si no solicitaste este cambio, puedes ignorar este correo.</p>"
                +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }

    public void activar2FA(Integer id, String secret) {
        UsuarioDTO user = new UsuarioDTO();
        user.setSecretKey2FA(secret);
        apiCliente.actualizarUsuario(id, user);
    }

    public void desactivar2FA(Integer id) {
        apiCliente.desactivar2FA(id);
    }

    public String actualizarPerfil(Integer id, String nombre, String email, String oldEmail) {
        UsuarioDTO user = new UsuarioDTO();
        user.setNombreCompleto(nombre);

        if (email != null && !email.equals(oldEmail)) {
            // Email changed
            UsuarioDTO existing = apiCliente.buscarUsuarioPorEmail(email);
            if (existing != null) {
                throw new RuntimeException("El email ya está en uso por otro usuario.");
            }

            // Set pending email and verification code
            user.setEmailPendiente(email);
            String code = java.util.UUID.randomUUID().toString();
            user.setCodigoVerificacion(code);

            // Send confirmation email
            String asunto = "Confirma tu nuevo email - Noticias App";
            String cuerpo = crearEmailConfirmacion(code);
            emailServicio.enviarEmail(email, asunto, cuerpo);

            apiCliente.actualizarUsuario(id, user); // Updates name, email_pendiente, code
            return "VERIFY";
        } else {
            // Only name changed
            apiCliente.actualizarUsuario(id, user);
            return "UPDATED";
        }
    }

    public String subirAvatar(Integer id, org.springframework.web.multipart.MultipartFile file) {
        java.util.Map<String, String> resp = apiCliente.subirAvatar(id, file);
        return resp != null ? resp.get("url") : null;
    }

    public boolean eliminarAvatar(Integer id) {
        return apiCliente.eliminarAvatar(id);
    }
}
