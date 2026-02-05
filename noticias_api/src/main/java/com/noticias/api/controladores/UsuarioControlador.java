package com.noticias.api.controladores;

import com.noticias.api.entidades.UsuarioEntidad;
import com.noticias.api.servicios.UsuarioServicio;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @author amorcia
 *         CLASE - Controlador REST para la gestión completa de usuarios del
 *         sistema.
 *         Proporciona endpoints para CRUD de usuarios, autenticación,
 *         recuperación de contraseña,
 *         gestión de vetados y operaciones de sesión. Incluye protección
 *         especial para el usuario OWNER.
 */
@RestController
@RequestMapping("/usuarios")
@CrossOrigin(origins = "*")
public class UsuarioControlador {

    private final UsuarioServicio usuarioServicio;
    private final com.noticias.api.servicios.AlmacenamientoServicio almacenamientoServicio;
    private final com.noticias.api.servicios.PrivilegiosServicio privilegiosServicio;

    /**
     * @author amorcia
     *         METODO - Constructor del controlador con inyección de dependencias
     * @param usuarioServicio        Servicio de lógica de negocio de usuarios
     * @param almacenamientoServicio Servicio para gestión de archivos e imágenes
     * @param privilegiosServicio    Servicio para verificación de permisos
     */
    public UsuarioControlador(UsuarioServicio usuarioServicio,
            com.noticias.api.servicios.AlmacenamientoServicio almacenamientoServicio,
            com.noticias.api.servicios.PrivilegiosServicio privilegiosServicio) {
        this.usuarioServicio = usuarioServicio;
        this.almacenamientoServicio = almacenamientoServicio;
        this.privilegiosServicio = privilegiosServicio;
    }

    /**
     * @author amorcia
     *         METODO - Verifica si un usuario es OWNER y retorna respuesta de error
     *         si lo es
     * @param id ID del usuario a verificar
     * @return ResponseEntity con error 403 si es OWNER, null si no lo es
     */
    private ResponseEntity<?> checkOwnerImmutability(Integer id) {
        UsuarioEntidad target = usuarioServicio.buscarPorId(id).orElse(null);
        if (target != null && target.getRol() != null && "OWNER".equalsIgnoreCase(target.getRol().getNombre())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return null;
    }

    /**
     * @author amorcia
     *         METODO - Obtiene la lista completa de todos los usuarios del sistema
     * @return ResponseEntity con lista de usuarios
     */
    @GetMapping
    public ResponseEntity<List<UsuarioEntidad>> listarTodos() {
        return ResponseEntity.ok(usuarioServicio.listarTodos());
    }

    /**
     * @author amorcia
     *         METODO - Busca un usuario específico por su ID
     * @param id ID del usuario a buscar
     * @return ResponseEntity con el usuario encontrado o 404 si no existe
     */
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioEntidad> buscarPorId(@PathVariable Integer id) {
        return usuarioServicio.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * @author amorcia
     *         METODO - Busca un usuario por su dirección de correo electrónico
     * @param email Email del usuario a buscar
     * @return ResponseEntity con el usuario encontrado o 404 si no existe
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<UsuarioEntidad> buscarPorEmail(@PathVariable String email) {
        return usuarioServicio.buscarPorEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * @author amorcia
     *         METODO - Crea un nuevo usuario en el sistema
     * @param usuario Entidad del usuario a crear
     * @return ResponseEntity con el usuario creado (201) o error (400)
     */
    @PostMapping
    public ResponseEntity<UsuarioEntidad> crear(@RequestBody UsuarioEntidad usuario) {
        try {
            UsuarioEntidad creado = usuarioServicio.crearUsuario(usuario);
            return ResponseEntity.status(HttpStatus.CREATED).body(creado);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * @author amorcia
     *         METODO - Actualiza los datos de un usuario existente. OWNER no puede
     *         ser modificado.
     * @param id      ID del usuario a actualizar
     * @param usuario Datos actualizados del usuario
     * @return ResponseEntity con usuario actualizado, 403 si es OWNER, o 404 si no
     *         existe
     */
    @PutMapping("/{id}")
    public ResponseEntity<UsuarioEntidad> actualizar(@PathVariable Integer id, @RequestBody UsuarioEntidad usuario) {
        ResponseEntity<?> ownerCheck = checkOwnerImmutability(id);
        if (ownerCheck != null)
            return (ResponseEntity<UsuarioEntidad>) ownerCheck;

        UsuarioEntidad actualizado = usuarioServicio.actualizarUsuario(id, usuario);
        if (actualizado != null) {
            return ResponseEntity.ok(actualizado);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * @author amorcia
     *         METODO - Confirma el email de un usuario mediante token de
     *         verificación
     * @param payload Mapa con el token de confirmación
     * @return ResponseEntity con mensaje de éxito o error
     */
    @PostMapping("/confirmar-email")
    public ResponseEntity<Map<String, String>> confirmarEmail(@RequestBody Map<String, String> payload) {
        String token = payload.get("token");
        boolean confirmado = usuarioServicio.confirmarEmail(token);
        if (confirmado) {
            return ResponseEntity.ok(Map.of("mensaje", "Email confirmado exitosamente"));
        }
        return ResponseEntity.badRequest().body(Map.of("error", "Token inválido o expirado"));
    }

    /**
     * @author amorcia
     *         METODO - Genera un token de recuperación de contraseña para un email
     * @param payload Mapa con el email del usuario
     * @return ResponseEntity con token generado o error si email no existe
     */
    @PostMapping("/recuperar-password")
    public ResponseEntity<Map<String, String>> recuperarPassword(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        String token = usuarioServicio.generarTokenRecuperacion(email);
        if (token != null) {
            return ResponseEntity.ok(Map.of("mensaje", "Token generado", "token", token));
        }
        return ResponseEntity.badRequest().body(Map.of("error", "Email no encontrado"));
    }

    /**
     * @author amorcia
     *         METODO - Restablece la contraseña de un usuario usando un token
     *         válido
     * @param payload Mapa con token y nuevaPassword
     * @return ResponseEntity con mensaje de éxito o error
     */
    @PostMapping("/restablecer-password")
    public ResponseEntity<Map<String, String>> restablecerPassword(@RequestBody Map<String, String> payload) {
        String token = payload.get("token");
        String nuevaPassword = payload.get("nuevaPassword");
        boolean restablecido = usuarioServicio.restablecerPassword(token, nuevaPassword);
        if (restablecido) {
            return ResponseEntity.ok(Map.of("mensaje", "Contraseña restablecida exitosamente"));
        }
        return ResponseEntity.badRequest().body(Map.of("error", "Token inválido o expirado"));
    }

    /**
     * @author amorcia
     *         METODO - Veta (banea) a un usuario del sistema. OWNER no puede ser
     *         vetado.
     * @param id      ID del usuario a vetar
     * @param payload Mapa con motivo y duración del veto
     * @return ResponseEntity con mensaje de éxito, 403 si es OWNER, o error
     */
    @PostMapping("/{id}/vetar")
    public ResponseEntity<Map<String, String>> vetarUsuario(@PathVariable Integer id,
            @RequestBody Map<String, String> payload) {
        ResponseEntity<?> ownerCheck = checkOwnerImmutability(id);
        if (ownerCheck != null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Cannot vet OWNER"));
        }

        String motivo = payload.get("motivo");
        String duracion = payload.get("duracion");
        boolean vetado = usuarioServicio.vetarUsuario(id, motivo, duracion);
        if (vetado) {
            return ResponseEntity.ok(Map.of("mensaje", "Usuario vetado exitosamente"));
        }
        return ResponseEntity.badRequest().body(Map.of("error", "No se pudo vetar el usuario"));
    }

    /**
     * @author amorcia
     *         METODO - Quita el veto (desbanea) a un usuario previamente vetado
     * @param id ID del usuario a desvetar
     * @return ResponseEntity con mensaje de éxito o error
     */
    @PostMapping("/{id}/desvetar")
    public ResponseEntity<Map<String, String>> desvetarUsuario(@PathVariable Integer id) {
        boolean desvetado = usuarioServicio.desvetarUsuario(id);
        if (desvetado) {
            return ResponseEntity.ok(Map.of("mensaje", "Usuario desvetado exitosamente"));
        }
        return ResponseEntity.badRequest().body(Map.of("error", "No se pudo desvetar el usuario"));
    }

    /**
     * @author amorcia
     *         METODO - Cierra la sesión activa de un usuario
     * @param id ID del usuario cuya sesión se cerrará
     * @return ResponseEntity vacío con 200 si éxito, 400 si error
     */
    @PostMapping("/{id}/logout")
    public ResponseEntity<Void> cerrarSesion(@PathVariable Integer id) {
        if (usuarioServicio.cerrarSesion(id)) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().build();
    }

    /**
     * @author amorcia
     *         METODO - Actualiza el token de sesión de un usuario. Permitido para
     *         todos incluyendo OWNER.
     * @param id      ID del usuario
     * @param payload Mapa con el nuevo token
     * @return ResponseEntity vacío con 200 si éxito, 400 si error
     */
    @PostMapping("/{id}/session-token")
    public ResponseEntity<Void> actualizarTokenSesion(@PathVariable Integer id,
            @RequestBody Map<String, String> payload) {
        String token = payload.get("token");
        if (usuarioServicio.actualizarTokenSesion(id, token)) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().build();
    }

    /**
     * @author amorcia
     *         METODO - Elimina un usuario con justificación detallada. OWNER no
     *         puede ser eliminado.
     * @param id      ID del usuario a eliminar
     * @param payload Mapa con motivo, descripción y eliminadorId
     * @return ResponseEntity con mensaje de éxito, 403 si es OWNER, o error
     */
    @PostMapping("/{id}/eliminar-con-justificacion")
    public ResponseEntity<Map<String, String>> eliminarConJustificacion(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> payload) {
        try {
            String motivo = (String) payload.get("motivo");
            String descripcion = (String) payload.get("descripcion");
            Integer eliminadorId = (Integer) payload.get("eliminadorId");

            ResponseEntity<?> ownerCheck = checkOwnerImmutability(id);
            if (ownerCheck != null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Cannot delete OWNER"));
            }

            if (motivo == null || motivo.isBlank() || eliminadorId == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Motivo y eliminadorId son requeridos"));
            }

            // Obtener usuario eliminador
            UsuarioEntidad eliminador = usuarioServicio.buscarPorId(eliminadorId).orElse(null);
            if (eliminador == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Eliminador no encontrado"));
            }

            // Obtener usuario objetivo
            UsuarioEntidad objetivo = usuarioServicio.buscarPorId(id).orElse(null);
            if (objetivo == null) {
                return ResponseEntity.notFound().build();
            }

            // Validar privilegios
            if (!privilegiosServicio.puedeEliminarUsuario(eliminador, objetivo)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "No tienes permisos para eliminar este usuario"));
            }

            // Eliminar con justificación
            boolean eliminado = usuarioServicio.eliminarUsuarioConJustificacion(id, motivo, descripcion, eliminador);
            if (eliminado) {
                return ResponseEntity.ok(Map.of("mensaje", "Usuario eliminado exitosamente"));
            }
            return ResponseEntity.badRequest().body(Map.of("error", "No se pudo eliminar el usuario"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al eliminar usuario: " + e.getMessage()));
        }
    }

    /**
     * @author amorcia
     *         METODO - Método deprecado para eliminar usuario sin justificación.
     *         OWNER no puede ser eliminado.
     * @deprecated Usar eliminarConJustificacion en su lugar
     * @param id ID del usuario a eliminar
     * @return ResponseEntity vacío con 204 si éxito, 403 si es OWNER, o 404 si no
     *         existe
     */
    @Deprecated
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        ResponseEntity<?> ownerCheck = checkOwnerImmutability(id);
        if (ownerCheck != null)
            return (ResponseEntity<Void>) ownerCheck;

        if (usuarioServicio.eliminarUsuario(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * @author amorcia
     *         METODO - Desactiva la autenticación de dos factores (2FA) para un
     *         usuario
     * @param id ID del usuario
     * @return ResponseEntity vacío con 200 si éxito, 400 si error
     */
    @PostMapping("/{id}/disable-2fa")
    public ResponseEntity<Void> desactivar2FA(@PathVariable Integer id) {
        if (usuarioServicio.desactivar2FA(id)) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().build();
    }

    /**
     * @author amorcia
     *         METODO - Sube una imagen de perfil para un usuario. OWNER no puede
     *         modificar su imagen.
     * @param id   ID del usuario
     * @param file Archivo de imagen a subir
     * @return ResponseEntity con URL de la imagen subida, 403 si es OWNER, o error
     */
    @PostMapping("/{id}/imagen")
    public ResponseEntity<Map<String, String>> subirImagen(@PathVariable Integer id,
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        ResponseEntity<?> ownerCheck = checkOwnerImmutability(id);
        if (ownerCheck != null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Cannot modify OWNER image"));
        }

        try {
            String url = almacenamientoServicio.almacenar(file);
            UsuarioEntidad u = new UsuarioEntidad();
            u.setImagenUrl(url);
            usuarioServicio.actualizarUsuario(id, u);
            return ResponseEntity.ok(Map.of("url", url));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", "Error al subir imagen"));
        }
    }

    /**
     * @author amorcia
     *         METODO - Elimina la imagen de perfil de un usuario. OWNER no puede
     *         modificar su imagen.
     * @param id ID del usuario
     * @return ResponseEntity vacío con 204 si éxito, 403 si es OWNER, o 400 si
     *         error
     */
    @DeleteMapping("/{id}/imagen")
    public ResponseEntity<Void> eliminarImagen(@PathVariable Integer id) {
        ResponseEntity<?> ownerCheck = checkOwnerImmutability(id);
        if (ownerCheck != null)
            return (ResponseEntity<Void>) ownerCheck;

        try {
            usuarioServicio.eliminarImagen(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
}
