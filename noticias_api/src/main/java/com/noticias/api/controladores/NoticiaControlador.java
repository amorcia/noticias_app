package com.noticias.api.controladores;

import com.noticias.api.entidades.NoticiaEntidad;
import com.noticias.api.servicios.NoticiaServicio;
import com.noticias.api.servicios.ModeracionServicio;
import com.noticias.api.servicios.AlmacenamientoServicio;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Controlador REST para gestión de noticias.
 */
@RestController
@RequestMapping("/noticias")
@CrossOrigin(origins = "*")
public class NoticiaControlador {

    private final NoticiaServicio noticiaServicio;
    private final ModeracionServicio moderacionServicio;
    private final AlmacenamientoServicio almacenamientoServicio;
    private final com.noticias.api.repositorios.UsuarioRepositorio usuarioRepositorio;

    public NoticiaControlador(NoticiaServicio noticiaServicio,
            ModeracionServicio moderacionServicio,
            AlmacenamientoServicio almacenamientoServicio,
            com.noticias.api.repositorios.UsuarioRepositorio usuarioRepositorio) {
        this.noticiaServicio = noticiaServicio;
        this.moderacionServicio = moderacionServicio;
        this.almacenamientoServicio = almacenamientoServicio;
        this.usuarioRepositorio = usuarioRepositorio;
    }

    @GetMapping
    public ResponseEntity<List<NoticiaEntidad>> listarTodas() {
        return ResponseEntity.ok(noticiaServicio.listarTodas());
    }

    @GetMapping("/destacadas")
    public ResponseEntity<List<NoticiaEntidad>> listarDestacadas() {
        return ResponseEntity.ok(noticiaServicio.listarDestacadas());
    }

    @GetMapping("/populares")
    public ResponseEntity<List<NoticiaEntidad>> listarPopulares() {
        return ResponseEntity.ok(noticiaServicio.listarPopulares());
    }

    @GetMapping("/categoria/{categoriaId}")
    public ResponseEntity<List<NoticiaEntidad>> listarPorCategoria(@PathVariable Integer categoriaId) {
        return ResponseEntity.ok(noticiaServicio.listarPorCategoria(categoriaId));
    }

    @GetMapping("/categoria/{categoriaId}/filtrar")
    public ResponseEntity<List<NoticiaEntidad>> listarPorCategoriaFiltrado(
            @PathVariable Integer categoriaId,
            @RequestParam(required = false) String filtro,
            @RequestParam(required = false) Integer mes,
            @RequestParam(required = false) Integer anio) {
        return ResponseEntity.ok(noticiaServicio.listarPorCategoriaFiltrado(categoriaId, filtro, mes, anio));
    }

    @GetMapping("/categoria/nombre/{nombre}")
    public ResponseEntity<List<NoticiaEntidad>> listarPorCategoriaNombre(@PathVariable String nombre) {
        return ResponseEntity.ok(noticiaServicio.listarPorCategoriaNombre(nombre));
    }

    @GetMapping("/categoria/nombre/{nombre}/foro")
    public ResponseEntity<List<NoticiaEntidad>> listarPorCategoriaForo(@PathVariable String nombre) {
        return ResponseEntity.ok(noticiaServicio.listarPorCategoriaNombreYTipo(nombre, true));
    }

    @GetMapping("/autor/{autorId}")
    public ResponseEntity<List<NoticiaEntidad>> listarPorAutor(@PathVariable Integer autorId) {
        return ResponseEntity.ok(noticiaServicio.listarPorAutor(autorId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<NoticiaEntidad> buscarPorId(@PathVariable Integer id) {
        // Incrementar visitas al ver la noticia
        noticiaServicio.incrementarVisitas(id);
        return noticiaServicio.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<NoticiaEntidad> crear(@RequestBody NoticiaEntidad noticia) {
        NoticiaEntidad creada = noticiaServicio.crearNoticia(noticia);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    @PostMapping(value = "/publicar", consumes = { "multipart/form-data" })
    public ResponseEntity<?> publicarNoticiaUsuario(
            @RequestParam("titulo") String titulo,
            @RequestParam("subtitulo") String subtitulo,
            @RequestParam("contenido") String contenido,
            @RequestParam("categoriaId") Integer categoriaId,
            @RequestParam("autorId") Integer autorId,
            @RequestParam("file") MultipartFile file) {

        try {
            // 1. Verificar Usuario
            if (autorId == null) {
                return ResponseEntity.badRequest().body("ID de autor es obligatorio");
            }
            com.noticias.api.entidades.UsuarioEntidad autor = usuarioRepositorio.findById(autorId)
                    .orElseThrow(() -> new RuntimeException("Autor no encontrado"));

            if (moderacionServicio.estaVetado(autor)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Usuario vetado. No puedes publicar.");
            }

            // 2. Moderación NSFW
            if (moderacionServicio.esContenidoNSFW(file)) {
                moderacionServicio.vetarUsuarioAutomaticamente(autor,
                        "Intento de subir contenido +18 detectado por IA.");
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Contenido inapropiado detectado. Has sido vetado por 7 días.");
            }

            // 3. Almacenar Archivo
            String imagenUrl = almacenamientoServicio.almacenar(file);

            // 4. Crear Noticia
            NoticiaEntidad noticia = new NoticiaEntidad();
            noticia.setTitulo(titulo);
            noticia.setSubtitulo(subtitulo);
            noticia.setContenido(contenido);
            noticia.setImagenUrl(imagenUrl);
            noticia.setEsAportacionUsuario(true);
            noticia.setAutor(autor);

            // Asignar categoría (simplificado, idealmente buscar entidad)
            com.noticias.api.entidades.CategoriaEntidad cat = new com.noticias.api.entidades.CategoriaEntidad();
            cat.setId(categoriaId);
            noticia.setCategoria(cat);

            NoticiaEntidad creada = noticiaServicio.crearNoticia(noticia);
            return ResponseEntity.status(HttpStatus.CREATED).body(creada);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al publicar: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<NoticiaEntidad> actualizar(@PathVariable Integer id, @RequestBody NoticiaEntidad noticia) {
        NoticiaEntidad actualizada = noticiaServicio.actualizarNoticia(id, noticia);
        if (actualizada != null) {
            return ResponseEntity.ok(actualizada);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/{id}/votar")
    public ResponseEntity<Void> votar(@PathVariable Integer id, @RequestParam Boolean like) {
        if (noticiaServicio.votar(id, like)) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id,
            @RequestParam(required = false) String motivo,
            @RequestParam(required = false) Integer eliminadorId) {

        com.noticias.api.entidades.UsuarioEntidad eliminador = null;
        if (eliminadorId != null) {
            eliminador = usuarioRepositorio.findById(eliminadorId).orElse(null);
        }

        if (noticiaServicio.eliminarNoticia(id, motivo, eliminador)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/eliminadas/admin")
    public ResponseEntity<List<com.noticias.api.entidades.NoticiaEliminadaEntidad>> listarEliminadasAdmin() {
        return ResponseEntity.ok(noticiaServicio.listarNoticiasEliminadasPorAdmin());
    }

    @GetMapping("/check-titulo")
    public ResponseEntity<Boolean> verificarTitulo(@RequestParam String titulo) {
        return ResponseEntity.ok(noticiaServicio.existePorTitulo(titulo));
    }
}
