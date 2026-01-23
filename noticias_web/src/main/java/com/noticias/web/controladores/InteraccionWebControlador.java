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
            Integer noticiaId = payload.get("noticiaId") != null ? Integer.valueOf(payload.get("noticiaId").toString())
                    : null;
            Integer usuarioId = payload.get("usuarioId") != null ? Integer.valueOf(payload.get("usuarioId").toString())
                    : null;
            String contenido = (String) payload.get("contenido");
            Integer padreId = payload.get("padreId") != null ? Integer.valueOf(payload.get("padreId").toString())
                    : null;

            if (noticiaId == null || usuarioId == null || contenido == null || contenido.isBlank()) {
                return ResponseEntity.badRequest().body("Datos incompletos");
            }

            ComentarioDTO comentario = new ComentarioDTO();
            comentario.setNoticiaId(noticiaId);
            comentario.setPadreId(padreId);

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
    public ResponseEntity<?> listarComentarios(@PathVariable Integer id,
            jakarta.servlet.http.HttpSession session) {
        try {
            com.noticias.web.dtos.UsuarioDTO usuario = (com.noticias.web.dtos.UsuarioDTO) session
                    .getAttribute("usuario");
            Integer usuarioId = usuario != null ? usuario.getId() : null;
            List<ComentarioDTO> comentarios = apiCliente.listarComentariosPorNoticia(id, usuarioId);
            return ResponseEntity.ok(comentarios);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error al obtener comentarios: " + e.getMessage());
        }
    }

    // Proxy para denuncias
    @PostMapping("/denuncias")
    public ResponseEntity<?> enviarDenuncia(@RequestBody Map<String, Object> payload) {
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

            boolean exito = apiCliente.enviarDenuncia(noticiaId, comentarioId, usuarioId, motivo, descripcion);

            if (exito)
                return ResponseEntity.ok().build();
            return ResponseEntity.status(500).body("Error al enviar denuncia");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/comentarios/{id}/votar")
    public ResponseEntity<?> votarComentario(@PathVariable Integer id, @RequestParam("tipo") String tipo,
            jakarta.servlet.http.HttpSession session) {
        try {
            com.noticias.web.dtos.UsuarioDTO usuario = (com.noticias.web.dtos.UsuarioDTO) session
                    .getAttribute("usuario");
            if (usuario == null) {
                return ResponseEntity.status(401).body("Debe iniciar sesión para votar");
            }
            boolean like = "LIKE".equalsIgnoreCase(tipo);
            apiCliente.votarComentario(id, like, usuario.getId());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error al votar: " + e.getMessage());
        }
    }

    @GetMapping("/noticia/{id}/voto")
    public ResponseEntity<Map<String, String>> obtenerVotoUsuario(@PathVariable Integer id,
            jakarta.servlet.http.HttpSession session) {
        com.noticias.web.dtos.UsuarioDTO usuario = (com.noticias.web.dtos.UsuarioDTO) session.getAttribute("usuario");
        if (usuario == null) {
            return ResponseEntity.ok(Map.of("voto", "NONE"));
        }
        String voto = apiCliente.obtenerVotoUsuario(id, usuario.getId());
        return ResponseEntity.ok(Map.of("voto", voto != null ? voto : "NONE"));
    }

    @PostMapping("/noticia/{id}/votar")
    public ResponseEntity<?> votarNoticia(@PathVariable Integer id, @RequestParam("tipo") String tipo,
            jakarta.servlet.http.HttpSession session) {
        try {
            com.noticias.web.dtos.UsuarioDTO usuario = (com.noticias.web.dtos.UsuarioDTO) session
                    .getAttribute("usuario");
            if (usuario == null) {
                return ResponseEntity.status(401).body("Debe iniciar sesión para votar");
            }
            boolean like = "LIKE".equalsIgnoreCase(tipo);
            apiCliente.votarNoticia(id, like, usuario.getId());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error al votar: " + e.getMessage());
        }
    }

    @DeleteMapping("/comentarios/{id}")
    public ResponseEntity<?> eliminarComentario(@PathVariable Integer id, jakarta.servlet.http.HttpSession session) {
        try {
            // Permission check could be done here or in apiCliente/API.
            // For now, let's assume API handles it or simple check for logged user.
            if (session.getAttribute("usuario") == null) {
                return ResponseEntity.status(401).body("No autenticado");
            }
            boolean ok = apiCliente.eliminarComentario(id);
            if (ok)
                return ResponseEntity.ok().build();
            return ResponseEntity.status(500).body("Error al eliminar comentario");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
}
