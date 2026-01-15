package com.noticias.api.controladores;

import com.noticias.api.entidades.UsuarioEntidad;
import com.noticias.api.servicios.UsuarioServicio;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controlador REST para gestión de usuarios.
 */
@RestController
@RequestMapping("/usuarios")
@CrossOrigin(origins = "*")
public class UsuarioControlador {

    private final UsuarioServicio usuarioServicio;
    private final com.noticias.api.servicios.AlmacenamientoServicio almacenamientoServicio;

    public UsuarioControlador(UsuarioServicio usuarioServicio,
            com.noticias.api.servicios.AlmacenamientoServicio almacenamientoServicio) {
        this.usuarioServicio = usuarioServicio;
        this.almacenamientoServicio = almacenamientoServicio;
    }

    @GetMapping
    public ResponseEntity<List<UsuarioEntidad>> listarTodos() {
        return ResponseEntity.ok(usuarioServicio.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioEntidad> buscarPorId(@PathVariable Integer id) {
        return usuarioServicio.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<UsuarioEntidad> buscarPorEmail(@PathVariable String email) {
        return usuarioServicio.buscarPorEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<UsuarioEntidad> crear(@RequestBody UsuarioEntidad usuario) {
        try {
            UsuarioEntidad creado = usuarioServicio.crearUsuario(usuario);
            return ResponseEntity.status(HttpStatus.CREATED).body(creado);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioEntidad> actualizar(@PathVariable Integer id, @RequestBody UsuarioEntidad usuario) {
        UsuarioEntidad actualizado = usuarioServicio.actualizarUsuario(id, usuario);
        if (actualizado != null) {
            return ResponseEntity.ok(actualizado);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/confirmar-email")
    public ResponseEntity<Map<String, String>> confirmarEmail(@RequestBody Map<String, String> payload) {
        String token = payload.get("token");
        boolean confirmado = usuarioServicio.confirmarEmail(token);
        if (confirmado) {
            return ResponseEntity.ok(Map.of("mensaje", "Email confirmado exitosamente"));
        }
        return ResponseEntity.badRequest().body(Map.of("error", "Token inválido o expirado"));
    }

    @PostMapping("/recuperar-password")
    public ResponseEntity<Map<String, String>> recuperarPassword(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        String token = usuarioServicio.generarTokenRecuperacion(email);
        if (token != null) {
            return ResponseEntity.ok(Map.of("mensaje", "Token generado", "token", token));
        }
        return ResponseEntity.badRequest().body(Map.of("error", "Email no encontrado"));
    }

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

    @PostMapping("/{id}/vetar")
    public ResponseEntity<Map<String, String>> vetarUsuario(@PathVariable Integer id,
            @RequestBody Map<String, String> payload) {
        String motivo = payload.get("motivo");
        boolean vetado = usuarioServicio.vetarUsuario(id, motivo);
        if (vetado) {
            return ResponseEntity.ok(Map.of("mensaje", "Usuario vetado exitosamente"));
        }
        return ResponseEntity.badRequest().body(Map.of("error", "No se pudo vetar el usuario"));
    }

    @PostMapping("/{id}/desvetar")
    public ResponseEntity<Map<String, String>> desvetarUsuario(@PathVariable Integer id) {
        boolean desvetado = usuarioServicio.desvetarUsuario(id);
        if (desvetado) {
            return ResponseEntity.ok(Map.of("mensaje", "Usuario desvetado exitosamente"));
        }
        return ResponseEntity.badRequest().body(Map.of("error", "No se pudo desvetar el usuario"));
    }

    @PostMapping("/{id}/logout")
    public ResponseEntity<Void> cerrarSesion(@PathVariable Integer id) {
        if (usuarioServicio.cerrarSesion(id)) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        if (usuarioServicio.eliminarUsuario(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/{id}/disable-2fa")
    public ResponseEntity<Void> desactivar2FA(@PathVariable Integer id) {
        if (usuarioServicio.desactivar2FA(id)) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().build();
    }

    @PostMapping("/{id}/imagen")
    public ResponseEntity<Map<String, String>> subirImagen(@PathVariable Integer id,
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
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

    @DeleteMapping("/{id}/imagen")
    public ResponseEntity<Void> eliminarImagen(@PathVariable Integer id) {
        try {
            UsuarioEntidad u = new UsuarioEntidad();
            u.setImagenUrl("");
            // Pass empty string (or special marker) or handle null in service.
            // Service expects null to skip update.
            // Let's modify service to handle empty string explicitly if needed, or pass
            // special logic.
            // Actually, let's just make service update null if we pass null?
            // But service null-checks to avoid overwriting with null.

            // Direct approach: find and save.
            usuarioServicio.eliminarImagen(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
}
