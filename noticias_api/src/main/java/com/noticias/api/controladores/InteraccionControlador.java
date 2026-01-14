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
    public ResponseEntity<List<ComentarioEntidad>> listarComentarios(@PathVariable Integer noticiaId) {
        return ResponseEntity.ok(comentarioServicio.listarPorNoticia(noticiaId));
    }

    @PostMapping("/comentarios")
    public ResponseEntity<?> crearComentario(@RequestBody Map<String, Object> payload) {
        try {
            Integer noticiaId = (Integer) payload.get("noticiaId");
            Integer usuarioId = (Integer) payload.get("usuarioId");
            String contenido = (String) payload.get("contenido");

            return ResponseEntity.ok(comentarioServicio.crearComentario(noticiaId, usuarioId, contenido));
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

    // --- Denuncias ---

    @PostMapping("/denuncias")
    public ResponseEntity<?> crearDenuncia(@RequestBody Map<String, Object> payload) {
        try {
            Integer noticiaId = (Integer) payload.get("noticiaId");
            Integer usuarioId = (Integer) payload.get("usuarioId");
            String motivo = (String) payload.get("motivo");
            String descripcion = (String) payload.get("descripcion");

            return ResponseEntity.ok(denunciaServicio.crearDenunciaNoticia(noticiaId, usuarioId, motivo, descripcion));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
