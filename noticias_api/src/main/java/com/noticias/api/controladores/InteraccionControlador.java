package com.noticias.api.controladores;

import com.noticias.api.entidades.ComentarioEntidad;
import com.noticias.api.servicios.ComentarioServicio;
import com.noticias.api.servicios.ComentarioServicio;
import com.noticias.api.servicios.DenunciaServicio;
import com.noticias.api.servicios.LoggerService; // Import
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @author amorcia
 *         CLASE - Controlador REST para gestión de interacciones de usuarios
 *         con noticias.
 *         Incluye comentarios (crear, listar, eliminar, votar) y denuncias de
 *         noticias/comentarios.
 */
@RestController
@RequestMapping("/interacciones")
@CrossOrigin(origins = "*")
public class InteraccionControlador {

    @Autowired
    private ComentarioServicio comentarioServicio;
    @Autowired
    private DenunciaServicio denunciaServicio;

    @Autowired
    private LoggerService logger; // Inject

    /**
     * @author amorcia
     *         METODO - Lista todos los comentarios de una noticia específica
     * @param noticiaId ID de la noticia
     * @param usuarioId ID del usuario (opcional, para marcar comentarios propios)
     * @return ResponseEntity con lista de comentarios
     */
    @GetMapping("/comentarios/noticia/{noticiaId}")
    public ResponseEntity<List<ComentarioEntidad>> listarComentarios(@PathVariable Integer noticiaId,
            @RequestParam(required = false) Integer usuarioId) {
        return ResponseEntity.ok(comentarioServicio.listarPorNoticia(noticiaId, usuarioId));
    }

    /**
     * @author amorcia
     *         METODO - Crea un nuevo comentario en una noticia
     * @param payload Mapa con noticiaId, usuarioId, contenido y padreId (opcional
     *                para respuestas)
     * @return ResponseEntity con el comentario creado o error
     */
    @PostMapping("/comentarios")
    public ResponseEntity<?> crearComentario(@RequestBody Map<String, Object> payload) {
        try {
            Integer noticiaId = payload.get("noticiaId") != null ? Integer.valueOf(payload.get("noticiaId").toString())
                    : null;
            Integer usuarioId = payload.get("usuarioId") != null ? Integer.valueOf(payload.get("usuarioId").toString())
                    : null;
            String contenido = (String) payload.get("contenido");
            Integer padreId = payload.get("padreId") != null ? Integer.valueOf(payload.get("padreId").toString())
                    : null;

            var result = comentarioServicio.crearComentario(noticiaId, usuarioId, contenido, padreId);

            // LOG
            try {
                // We don't have email here directly, but we have usuarioId.
                // Ideally ComentarioServicio should return the created comment with author
                // info.
                // Or we fetch user name. Simplified logging for now (just ID).
                if (usuarioId != null) {
                    logger.logAction("user_id_" + usuarioId, "COMMENT", "Commented on news " + noticiaId);
                }
            } catch (Exception e) {
            }

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * @author amorcia
     *         METODO - Elimina un comentario (solo el autor o admin)
     * @param id        ID del comentario a eliminar
     * @param usuarioId ID del usuario que solicita la eliminación
     * @param esAdmin   Indica si el usuario es administrador
     * @return ResponseEntity vacío con 200 si éxito, 403 si no tiene permisos
     */
    @DeleteMapping("/comentarios/{id}")
    public ResponseEntity<?> eliminarComentario(@PathVariable Integer id,
            @RequestParam Integer usuarioId,
            @RequestParam boolean esAdmin) {
        try {
            comentarioServicio.eliminarComentario(id, usuarioId, esAdmin);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(403).body(e.getMessage());
        }
    }

    /**
     * @author amorcia
     *         METODO - Registra un voto (like/dislike) en un comentario
     * @param id        ID del comentario
     * @param like      true para like, false para dislike
     * @param usuarioId ID del usuario que vota
     * @return ResponseEntity vacío con 200 si éxito, 404 si comentario no existe
     */
    @PostMapping("/comentarios/{id}/votar")
    public ResponseEntity<Void> votarComentario(@PathVariable Integer id, @RequestParam Boolean like,
            @RequestParam Integer usuarioId) {
        if (comentarioServicio.votar(id, like, usuarioId)) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * @author amorcia
     *         METODO - Crea una denuncia sobre una noticia o comentario
     * @param payload Mapa con noticiaId o comentarioId, usuarioId, motivo y
     *                descripción
     * @return ResponseEntity con la denuncia creada o error
     */
    @PostMapping("/denuncias")
    public ResponseEntity<?> crearDenuncia(@RequestBody Map<String, Object> payload) {
        try {
            Integer noticiaId = payload.get("noticiaId") != null ? Integer.valueOf(payload.get("noticiaId").toString())
                    : null;
            Integer comentarioId = payload.get("comentarioId") != null
                    ? Integer.valueOf(payload.get("comentarioId").toString())
                    : null;
            Integer usuarioId = payload.get("usuarioId") != null ? Integer.valueOf(payload.get("usuarioId").toString())
                    : null;
            String motivo = (String) payload.get("motivo");
            String descripcion = (String) payload.get("descripcion");

            if (comentarioId != null) {
                return ResponseEntity
                        .ok(denunciaServicio.crearDenunciaComentario(comentarioId, usuarioId, motivo, descripcion));
            } else {
                return ResponseEntity
                        .ok(denunciaServicio.crearDenunciaNoticia(noticiaId, usuarioId, motivo, descripcion));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * @author amorcia
     *         METODO - Lista todas las denuncias del sistema
     * @return ResponseEntity con lista de denuncias
     */
    @GetMapping("/denuncias")
    public ResponseEntity<?> listarDenuncias() {
        return ResponseEntity.ok(denunciaServicio.listarTodas());
    }
}
