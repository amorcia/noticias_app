package com.noticias.api.controladores;

import com.noticias.api.dtos.NoticiaDTO;
import com.noticias.api.entidades.NoticiaEntidad;
import com.noticias.api.entidades.NoticiaEliminadaEntidad;
import com.noticias.api.entidades.UsuarioEntidad;
import com.noticias.api.repositorios.UsuarioRepositorio;
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
    private final UsuarioRepositorio usuarioRepositorio;

    public NoticiaControlador(NoticiaServicio noticiaServicio,
            ModeracionServicio moderacionServicio,
            AlmacenamientoServicio almacenamientoServicio,
            UsuarioRepositorio usuarioRepositorio) {
        this.noticiaServicio = noticiaServicio;
        this.moderacionServicio = moderacionServicio;
        this.almacenamientoServicio = almacenamientoServicio;
        this.usuarioRepositorio = usuarioRepositorio;
    }

    @GetMapping
    public ResponseEntity<List<NoticiaDTO>> listarTodas() {
        return ResponseEntity.ok(noticiaServicio.listarTodas());
    }

    @GetMapping("/destacadas")
    public ResponseEntity<List<NoticiaDTO>> listarDestacadas() {
        return ResponseEntity.ok(noticiaServicio.listarDestacadas());
    }

    @GetMapping("/populares")
    public ResponseEntity<List<NoticiaDTO>> listarPopulares() {
        return ResponseEntity.ok(noticiaServicio.listarPopulares());
    }

    @GetMapping("/categoria/{categoriaId}")
    public ResponseEntity<List<NoticiaDTO>> listarPorCategoria(@PathVariable Integer categoriaId) {
        return ResponseEntity.ok(noticiaServicio.listarPorCategoria(categoriaId));
    }

    @GetMapping("/categoria/{categoriaId}/filtrar")
    public ResponseEntity<List<NoticiaDTO>> listarPorCategoriaFiltrado(
            @PathVariable Integer categoriaId,
            @RequestParam(required = false) String filtro,
            @RequestParam(required = false) Integer mes,
            @RequestParam(required = false) Integer anio) {
        return ResponseEntity.ok(noticiaServicio.listarPorCategoriaFiltrado(categoriaId, filtro, mes, anio));
    }

    @GetMapping("/categoria/nombre/{nombre}")
    public ResponseEntity<List<NoticiaDTO>> listarPorCategoriaNombre(@PathVariable String nombre) {
        return ResponseEntity.ok(noticiaServicio.listarPorCategoriaNombre(nombre));
    }

    @GetMapping("/categoria/nombre/{nombre}/foro")
    public ResponseEntity<List<NoticiaDTO>> listarPorCategoriaForo(@PathVariable String nombre) {
        return ResponseEntity.ok(noticiaServicio.listarPorCategoriaNombreYTipo(nombre, true));
    }

    @GetMapping("/autor/{autorId}")
    public ResponseEntity<List<NoticiaDTO>> listarPorAutor(@PathVariable Integer autorId) {
        return ResponseEntity.ok(noticiaServicio.listarPorAutor(autorId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<NoticiaDTO> buscarPorId(@PathVariable Integer id) {
        // Incrementar visitas al ver la noticia
        noticiaServicio.incrementarVisitas(id);
        return noticiaServicio.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/titulo")
    public ResponseEntity<NoticiaDTO> buscarPorTitulo(@RequestParam String titulo) {
        return noticiaServicio.buscarPorTitulo(titulo)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<NoticiaDTO> crear(@RequestBody NoticiaEntidad noticia) {
        NoticiaDTO creada = noticiaServicio.crearNoticia(noticia);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    @PostMapping(value = "/publicar", consumes = { "multipart/form-data" })
    public ResponseEntity<?> publicarNoticiaUsuario(
            @RequestParam("titulo") String titulo,
            @RequestParam("subtitulo") String subtitulo,
            @RequestParam("contenido") String contenido,
            @RequestParam("categoriaId") Integer categoriaId,
            @RequestParam("autorId") Integer autorId,
            @RequestParam(value = "file", required = false) MultipartFile file) {

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

            // 2. Validación y procesamiento de imagen
            String imagenUrl = null;
            if (file != null && !file.isEmpty()) {
                // Validar tipo y tamaño (lanza IllegalArgumentException si falla)
                almacenamientoServicio.validarTipoImagen(file);
                almacenamientoServicio.validarTamañoArchivo(file);

                // 3. Moderación NSFW
                if (moderacionServicio.esContenidoNSFW(file)) {
                    moderacionServicio.vetarUsuarioAutomaticamente(autor,
                            "Intento de subir contenido +18 detectado por IA.");
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body("Contenido inapropiado detectado. Has sido vetado por 7 días.");
                }

                // 4. Almacenar y optimizar archivo
                imagenUrl = almacenamientoServicio.almacenar(file);
            }

            // 5. Crear Noticia
            NoticiaEntidad noticia = new NoticiaEntidad();
            noticia.setTitulo(titulo);
            noticia.setSubtitulo(subtitulo);
            noticia.setContenido(contenido);
            noticia.setImagenUrl(imagenUrl);

            // Determinar si es aportación de usuario (foro) o noticia oficial
            String rolNombre = autor.getRol() != null ? autor.getRol().getNombre() : "";
            boolean esOficial = "TRABAJADOR".equalsIgnoreCase(rolNombre) ||
                    "ADMIN".equalsIgnoreCase(rolNombre) ||
                    "OWNER".equalsIgnoreCase(rolNombre);
            noticia.setEsAportacionUsuario(!esOficial);
            noticia.setAutor(autor);

            // Asignar categoría
            com.noticias.api.entidades.CategoriaEntidad cat = new com.noticias.api.entidades.CategoriaEntidad();
            cat.setId(categoriaId);
            noticia.setCategoria(cat);

            NoticiaDTO creada = noticiaServicio.crearNoticia(noticia);
            return ResponseEntity.status(HttpStatus.CREATED).body(creada);

        } catch (IllegalArgumentException e) {
            // Errores de validación (tipo de archivo, tamaño, etc.)
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al publicar: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<NoticiaDTO> actualizar(@PathVariable Integer id, @RequestBody NoticiaEntidad noticia) {
        NoticiaDTO actualizada = noticiaServicio.actualizarNoticia(id, noticia);
        if (actualizada != null) {
            return ResponseEntity.ok(actualizada);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping(value = "/{id}/editar", consumes = { "multipart/form-data" })
    public ResponseEntity<?> editarNoticia(
            @PathVariable Integer id,
            @RequestParam("titulo") String titulo,
            @RequestParam("subtitulo") String subtitulo,
            @RequestParam("contenido") String contenido,
            @RequestParam("categoriaId") Integer categoriaId,
            @RequestParam(value = "file", required = false) MultipartFile file) {

        try {
            // 1. Buscar noticia
            NoticiaDTO noticiaExistente = noticiaServicio.buscarPorId(id).orElse(null);
            if (noticiaExistente == null) {
                return ResponseEntity.notFound().build();
            }

            // 2. Procesar imagen nueva si existe
            String imagenUrl = noticiaExistente.getImagenUrl();
            if (file != null && !file.isEmpty()) {
                almacenamientoServicio.validarTipoImagen(file);
                almacenamientoServicio.validarTamañoArchivo(file);

                // Moderación NSFW
                if (moderacionServicio.esContenidoNSFW(file)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body("Contenido inapropiado detectado en la nueva imagen.");
                }

                imagenUrl = almacenamientoServicio.almacenar(file);
            }

            // 3. Preparar entidad para actualización
            NoticiaEntidad noticiaUpdate = new NoticiaEntidad();
            noticiaUpdate.setTitulo(titulo);
            noticiaUpdate.setSubtitulo(subtitulo);
            noticiaUpdate.setContenido(contenido);
            noticiaUpdate.setImagenUrl(imagenUrl);

            com.noticias.api.entidades.CategoriaEntidad cat = new com.noticias.api.entidades.CategoriaEntidad();
            cat.setId(categoriaId);
            noticiaUpdate.setCategoria(cat);

            NoticiaDTO actualizada = noticiaServicio.actualizarNoticia(id, noticiaUpdate);
            return ResponseEntity.ok(actualizada);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al editar: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/votar")
    public ResponseEntity<Void> votar(@PathVariable Integer id, @RequestParam Boolean like,
            @RequestParam Integer usuarioId) {
        if (noticiaServicio.votar(id, like, usuarioId)) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}/voto")
    public ResponseEntity<java.util.Map<String, String>> obtenerVoto(@PathVariable Integer id,
            @RequestParam Integer usuarioId) {
        String tipo = noticiaServicio.obtenerTipoVoto(id, usuarioId);
        return ResponseEntity.ok(java.util.Map.of("tipo", tipo != null ? tipo : "NONE"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id,
            @RequestParam(required = false) String motivo,
            @RequestParam(required = false) String descripcion,
            @RequestParam(required = false) Integer eliminadorId) {

        UsuarioEntidad eliminador = null;
        if (eliminadorId != null) {
            eliminador = usuarioRepositorio.findById(eliminadorId).orElse(null);
        }

        if (noticiaServicio.eliminarNoticia(id, motivo, descripcion, eliminador)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/titulo")
    public ResponseEntity<Void> eliminarPorTitulo(@RequestParam String titulo,
            @RequestParam(required = false) String motivo,
            @RequestParam(required = false) String descripcion,
            @RequestParam(required = false) Integer eliminadorId) {

        UsuarioEntidad eliminador = null;
        if (eliminadorId != null) {
            eliminador = usuarioRepositorio.findById(eliminadorId).orElse(null);
        }

        if (noticiaServicio.eliminarNoticiaPorTitulo(titulo, motivo, descripcion, eliminador)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/eliminadas/admin")
    public ResponseEntity<List<NoticiaEliminadaEntidad>> listarEliminadasAdmin() {
        return ResponseEntity.ok(noticiaServicio.listarNoticiasEliminadasPorAdmin());
    }

    @GetMapping("/eliminadas/{id}")
    public ResponseEntity<NoticiaEliminadaEntidad> obtenerEliminadaPorId(@PathVariable Long id) {
        return noticiaServicio.obtenerNoticiaEliminadaPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/check-titulo")
    public ResponseEntity<Boolean> verificarTitulo(@RequestParam String titulo) {
        return ResponseEntity.ok(noticiaServicio.existePorTitulo(titulo));
    }
}
