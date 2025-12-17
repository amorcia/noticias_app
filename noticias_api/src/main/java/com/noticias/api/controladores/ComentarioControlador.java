package com.noticias.api.controladores;

import com.noticias.api.entidades.ComentarioEntidad;
import com.noticias.api.servicios.ComentarioServicio;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gestión de comentarios.
 */
@RestController
@RequestMapping("/comentarios")
@CrossOrigin(origins = "*")
public class ComentarioControlador {

    private final ComentarioServicio comentarioServicio;

    public ComentarioControlador(ComentarioServicio comentarioServicio) {
        this.comentarioServicio = comentarioServicio;
    }

    @GetMapping("/noticia/{noticiaId}")
    public ResponseEntity<List<ComentarioEntidad>> listarPorNoticia(@PathVariable Integer noticiaId) {
        return ResponseEntity.ok(comentarioServicio.listarPorNoticia(noticiaId));
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<ComentarioEntidad>> listarPorUsuario(@PathVariable Integer usuarioId) {
        return ResponseEntity.ok(comentarioServicio.listarPorUsuario(usuarioId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ComentarioEntidad> buscarPorId(@PathVariable Integer id) {
        return comentarioServicio.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ComentarioEntidad> crear(@RequestBody ComentarioEntidad comentario) {
        ComentarioEntidad creado = comentarioServicio.crearComentario(comentario);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        if (comentarioServicio.eliminarComentario(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
