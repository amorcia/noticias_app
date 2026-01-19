package com.noticias.web.controladores;

import com.noticias.web.dtos.ComentarioDTO;
import com.noticias.web.servicios.ApiNoticiasCliente;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/interacciones")
public class InteraccionWebControlador {

    private final ApiNoticiasCliente apiCliente;

    public InteraccionWebControlador(ApiNoticiasCliente apiCliente) {
        this.apiCliente = apiCliente;
    }

    @PostMapping("/comentarios")
    public ResponseEntity<?> publicarComentario(@RequestBody Map<String, Object> payload) {
        try {
            Integer noticiaId = (Integer) payload.get("noticiaId");
            Integer usuarioId = (Integer) payload.get("usuarioId");
            String contenido = (String) payload.get("contenido");

            if (noticiaId == null || usuarioId == null || contenido == null || contenido.isBlank()) {
                return ResponseEntity.badRequest().body("Datos incompletos");
            }

            ComentarioDTO comentario = new ComentarioDTO();
            comentario.setNoticiaId(noticiaId);

            // Create nested AutorDTO
            ComentarioDTO.AutorDTO autor = new ComentarioDTO.AutorDTO();
            autor.setId(usuarioId);
            comentario.setAutor(autor);

            comentario.setContenido(contenido);

            ComentarioDTO creado = apiCliente.crearComentario(comentario);
            if (creado != null) {
                return ResponseEntity.ok(creado);
            } else {
                return ResponseEntity.status(500).body("Error al crear comentario");
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error interno: " + e.getMessage());
        }
    }

    @GetMapping("/comentarios/noticia/{id}")
    public ResponseEntity<List<ComentarioDTO>> listarComentarios(@PathVariable Integer id) {
        List<ComentarioDTO> comentarios = apiCliente.listarComentariosPorNoticia(id);
        return ResponseEntity.ok(comentarios);
    }

    // Proxy para denuncias
    @PostMapping("/denuncias")
    public ResponseEntity<?> enviarDenuncia(@RequestBody Map<String, Object> payload) {
        try {
            Integer noticiaId = (Integer) payload.get("noticiaId");
            Integer usuarioId = (Integer) payload.get("usuarioId");
            String motivo = (String) payload.get("motivo");
            String descripcion = (String) payload.get("descripcion");

            // Assuming ApiCliente has a method for this, OR we use RestTemplate here
            // directly if client lacks it.
            // Client likely lacks 'crearDenuncia'. Let's add it to client or do direct call
            // if lazy.
            // Best practice: Add to Client.

            boolean exito = apiCliente.enviarDenuncia(noticiaId, usuarioId, motivo, descripcion);

            if (exito)
                return ResponseEntity.ok().build();
            return ResponseEntity.status(500).body("Error al enviar denuncia");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/noticia/{id}/votar")
    public ResponseEntity<?> votarNoticia(@PathVariable Integer id, @RequestParam("tipo") String tipo) {
        try {
            boolean like = "LIKE".equalsIgnoreCase(tipo);
            apiCliente.votarNoticia(id, like);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error al votar");
        }
    }
}
