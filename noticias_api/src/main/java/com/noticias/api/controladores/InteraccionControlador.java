package com.noticias.api.controladores;

import com.noticias.api.entidades.ComentarioEntidad;
import com.noticias.api.servicios.ComentarioServicio;
import com.noticias.api.servicios.DenunciaServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/interacciones")
@CrossOrigin(origins = "*")
public class InteraccionControlador {

    @Autowired
    private ComentarioServicio comentarioServicio;
    @Autowired
    private DenunciaServicio denunciaServicio;

    // --- Comentarios ---

    @GetMapping("/comentarios/noticia/{noticiaId}")
    public ResponseEntity<List<ComentarioEntidad>> listarComentarios(@PathVariable Integer noticiaId,
            @RequestParam(required = false) Integer usuarioId) {
        return ResponseEntity.ok(comentarioServicio.listarPorNoticia(noticiaId, usuarioId));
    }

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

            return ResponseEntity.ok(comentarioServicio.crearComentario(noticiaId, usuarioId, contenido, padreId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

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

    @PostMapping("/comentarios/{id}/votar")
    public ResponseEntity<Void> votarComentario(@PathVariable Integer id, @RequestParam Boolean like,
            @RequestParam Integer usuarioId) {
        if (comentarioServicio.votar(id, like, usuarioId)) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    // --- Denuncias ---

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

    @GetMapping("/denuncias")
    public ResponseEntity<?> listarDenuncias() {
        return ResponseEntity.ok(denunciaServicio.listarTodas());
    }
}
