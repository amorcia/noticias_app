package com.noticias.api.controladores;

import com.noticias.api.dtos.NoticiaDTO;
import com.noticias.api.entidades.NoticiaEntidad;
import com.noticias.api.entidades.NoticiaEliminadaEntidad;
import com.noticias.api.entidades.UsuarioEntidad;
import com.noticias.api.repositorios.UsuarioRepositorio;
import com.noticias.api.servicios.NoticiaServicio;
import com.noticias.api.servicios.ModeracionServicio;
import com.noticias.api.servicios.AlmacenamientoServicio;
import com.noticias.api.servicios.LoggerService;
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
    private final LoggerService loggerService;

    public NoticiaControlador(NoticiaServicio noticiaServicio,
            ModeracionServicio moderacionServicio,
            AlmacenamientoServicio almacenamientoServicio,
            UsuarioRepositorio usuarioRepositorio,
            LoggerService loggerService) {
        this.noticiaServicio = noticiaServicio;
        this.moderacionServicio = moderacionServicio;
        this.almacenamientoServicio = almacenamientoServicio;
        this.usuarioRepositorio = usuarioRepositorio;
        this.loggerService = loggerService;
    }

    @GetMapping
    public ResponseEntity<List<NoticiaDTO>> listarTodas() {
        return ResponseEntity.ok(noticiaServicio.listarTodas());
    }

    @GetMapping("/destacadas")
    public ResponseEntity<List<NoticiaDTO>> listarDestacadas() {
        return ResponseEntity.ok(noticiaServicio.listarDestacadas());
    }

    /**
     * @author amorcia
     *         METODO - Lista noticias populares (más visitadas)
     * @return ResponseEntity con lista de noticias populares
     */
    @GetMapping("/populares")
    public ResponseEntity<List<NoticiaDTO>> listarPopulares() {
        return ResponseEntity.ok(noticiaServicio.listarPopulares());
    }

    /**
     * @author amorcia
     *         METODO - Filtra noticias por categoría y criterios opcionales (texto,
     *         fecha)
     * @param categoriaId ID de la categoría
     * @param filtro      Texto a buscar en título/contenido (opcional)
     * @param mes         Mes de publicación (opcional)
     * @param anio        Año de publicación (opcional)
     * @return ResponseEntity con lista de noticias filtradas
     */
    @GetMapping("/categoria/{categoriaId}/filtrar")
    public ResponseEntity<List<NoticiaDTO>> listarPorCategoriaFiltrado(
            @PathVariable Integer categoriaId,
            @RequestParam(required = false) String filtro,
            @RequestParam(required = false) Integer mes,
            @RequestParam(required = false) Integer anio) {
        return ResponseEntity.ok(noticiaServicio.listarPorCategoriaFiltrado(categoriaId, filtro, mes, anio));
    }

    /**
     * @author amorcia
     *         METODO - Lista noticias por nombre de categoría
     * @param nombre Nombre de la categoría
     * @return ResponseEntity con lista de noticias
     */
    @GetMapping("/categoria/nombre/{nombre}")
    public ResponseEntity<List<NoticiaDTO>> listarPorCategoriaNombre(@PathVariable String nombre) {
        return ResponseEntity.ok(noticiaServicio.listarPorCategoriaNombre(nombre));
    }

    /**
     * @author amorcia
     *         METODO - Lista noticias tipo foro (aportaciones de usuarios) de una
     *         categoría
     * @param nombre Nombre de la categoría
     * @return ResponseEntity con lista de noticias del foro
     */
    @GetMapping("/categoria/{categoriaId}")
    public ResponseEntity<List<NoticiaDTO>> listarPorCategoria(@PathVariable Integer categoriaId) {
        return ResponseEntity.ok(noticiaServicio.listarPorCategoria(categoriaId));
    }

    /**
     * @author amorcia
     *         METODO - Lista noticias tipo foro (aportaciones de usuarios) de una
     *         categoría
     * @param nombre Nombre de la categoría
     * @return ResponseEntity con lista de noticias del foro
     */
    @GetMapping("/categoria/nombre/{nombre}/foro")
    public ResponseEntity<List<NoticiaDTO>> listarPorCategoriaForo(@PathVariable String nombre) {
        return ResponseEntity.ok(noticiaServicio.listarPorCategoriaNombreYTipo(nombre, true));
    }

    /**
     * @author amorcia
     *         METODO - Lista las 5 noticias mejor reaccionadas de una categoría
     * @param categoriaId ID de la categoría
     * @return ResponseEntity con lista de tendencias
     */
    @GetMapping("/categoria/{categoriaId}/tendencias")
    public ResponseEntity<List<NoticiaDTO>> listarTendenciasPorCategoria(@PathVariable Integer categoriaId) {
        return ResponseEntity.ok(noticiaServicio.listarTendenciasPorCategoria(categoriaId));
    }

    /**
     * @author amorcia
     *         METODO - Búsqueda de noticias en tiempo real (Live Search)
     * @param titulo Texto a buscar en el título
     * @return ResponseEntity con lista de noticias coincidentes
     */
    @GetMapping("/buscar/live")
    public ResponseEntity<List<NoticiaDTO>> buscarLive(@RequestParam String titulo) {
        return ResponseEntity.ok(noticiaServicio.buscarNoticiasLive(titulo));
    }

    /**
     * @author amorcia
     *         METODO - Lista noticias de un autor específico
     * @param autorId ID del autor
     * @return ResponseEntity con lista de noticias
     */
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

    /**
     * @author amorcia
     *         METODO - Busca una noticia por su título exacto
     * @param titulo Título a buscar
     * @return ResponseEntity con la noticia o 404
     */
    @GetMapping("/titulo")
    public ResponseEntity<NoticiaDTO> buscarPorTitulo(@RequestParam String titulo) {
        return noticiaServicio.buscarPorTitulo(titulo)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * @author amorcia
     *         METODO - Crea una noticia directamente (uso interno/admin)
     * @param noticia Entidad noticia
     * @return ResponseEntity con noticia creada
     */
    @PostMapping
    public ResponseEntity<NoticiaDTO> crear(@RequestBody NoticiaEntidad noticia) {
        NoticiaDTO creada = noticiaServicio.crearNoticia(noticia);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    /**
     * @author amorcia
     *         METODO - Helper para procesar imagen (validación, moderación,
     *         almacenamiento)
     * @param file  Archivo de imagen
     * @param autor Autor (para vetar si es NSFW)
     * @return URL de la imagen o mensaje de error si comienza con "ERROR:"
     * @throws Exception Si hay error de IO
     */
    private String procesarImagen(MultipartFile file, UsuarioEntidad autor) throws Exception {
        if (file == null || file.isEmpty())
            return null;

        almacenamientoServicio.validarTipoImagen(file);
        almacenamientoServicio.validarTamañoArchivo(file);

        if (moderacionServicio.esContenidoNSFW(file)) {
            if (autor != null) {
                moderacionServicio.vetarUsuarioAutomaticamente(autor,
                        "Intento de subir contenido +18 detectado por IA.");
            }
            return "ERROR: Contenido inapropiado detectado.";
        }
        return almacenamientoServicio.almacenar(file);
    }

    /**
     * @author amorcia
     *         METODO - Publica una noticia desde el formulario de usuario con
     *         validaciones y subida de imagen
     * @param titulo      Título de la noticia
     * @param subtitulo   Subtítulo
     * @param contenido   Contenido HTML/Texto
     * @param categoriaId ID de la categoría
     * @param autorId     ID del autor
     * @param file        Archivo de imagen (opcional)
     * @return ResponseEntity con resultado
     */
    @PostMapping(value = "/publicar", consumes = { "multipart/form-data" })
    public ResponseEntity<?> publicarNoticiaUsuario(
            @RequestParam("titulo") String titulo,
            @RequestParam("subtitulo") String subtitulo,
            @RequestParam("contenido") String contenido,
            @RequestParam("categoriaId") Integer categoriaId,
            @RequestParam("autorId") Integer autorId,
            @RequestParam(value = "file", required = false) MultipartFile file) {

        try {
            if (autorId == null)
                return ResponseEntity.badRequest().body("ID de autor es obligatorio");

            UsuarioEntidad autor = usuarioRepositorio.findById(autorId)
                    .orElseThrow(() -> new RuntimeException("Autor no encontrado"));

            if (moderacionServicio.estaVetado(autor)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Usuario vetado. No puedes publicar.");
            }

            String imagenUrlResult = procesarImagen(file, autor);
            if (imagenUrlResult != null && imagenUrlResult.startsWith("ERROR:")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(imagenUrlResult.substring(7));
            }
            String imagenUrl = imagenUrlResult;

            NoticiaEntidad noticia = new NoticiaEntidad();
            noticia.setTitulo(titulo);
            noticia.setSubtitulo(subtitulo);
            noticia.setContenido(contenido);
            noticia.setImagenUrl(imagenUrl);

            String rolNombre = autor.getRol() != null ? autor.getRol().getNombre() : "";
            boolean esOficial = "TRABAJADOR".equalsIgnoreCase(rolNombre) ||
                    "ADMIN".equalsIgnoreCase(rolNombre) ||
                    "OWNER".equalsIgnoreCase(rolNombre);
            noticia.setEsAportacionUsuario(!esOficial);
            noticia.setAutor(autor);

            com.noticias.api.entidades.CategoriaEntidad cat = new com.noticias.api.entidades.CategoriaEntidad();
            cat.setId(categoriaId);
            noticia.setCategoria(cat);

            NoticiaDTO creada = noticiaServicio.crearNoticia(noticia);
            return ResponseEntity.status(HttpStatus.CREATED).body(creada);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al publicar: " + e.getMessage());
        }
    }

    /**
     * @author amorcia
     *         METODO - Actualiza una noticia existente
     * @param id      ID de la noticia
     * @param noticia Datos actualizados
     * @return ResponseEntity con noticia actualizada
     */
    @PutMapping("/{id}")
    public ResponseEntity<NoticiaDTO> actualizar(@PathVariable Integer id, @RequestBody NoticiaEntidad noticia) {
        NoticiaDTO actualizada = noticiaServicio.actualizarNoticia(id, noticia);
        if (actualizada != null) {
            return ResponseEntity.ok(actualizada);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * @author amorcia
     *         METODO - Edita una noticia con soporte para nueva imagen
     * @param id          ID de la noticia
     * @param titulo      Nuevo título
     * @param subtitulo   Nuevo subtítulo
     * @param contenido   Nuevo contenido
     * @param categoriaId Nueva categoría
     * @param file        Nueva imagen (opcional)
     * @return ResponseEntity con noticia editada
     */
    @PostMapping(value = "/{id}/editar", consumes = { "multipart/form-data" })
    public ResponseEntity<?> editarNoticia(
            @PathVariable Integer id,
            @RequestParam("titulo") String titulo,
            @RequestParam("subtitulo") String subtitulo,
            @RequestParam("contenido") String contenido,
            @RequestParam("categoriaId") Integer categoriaId,
            @RequestParam(value = "file", required = false) MultipartFile file) {

        try {
            NoticiaDTO noticiaExistente = noticiaServicio.buscarPorId(id).orElse(null);
            if (noticiaExistente == null)
                return ResponseEntity.notFound().build();

            String imagenUrlResult = procesarImagen(file, null); // No author check on edit for now or pass context if
                                                                 // needed
            if (imagenUrlResult != null && imagenUrlResult.startsWith("ERROR:")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(imagenUrlResult.substring(7));
            }
            // Keep old image if new one is not provided, else use new one
            String imagenUrl = (imagenUrlResult != null) ? imagenUrlResult : noticiaExistente.getImagenUrl();

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

    /**
     * @author amorcia
     *         METODO - Registra un voto (like/dislike) en una noticia
     * @param id        ID de la noticia
     * @param like      true para like, false para dislike
     * @param usuarioId ID del usuario
     * @return ResponseEntity vacío con 200 si éxito, 404 si no existe
     */
    @PostMapping("/{id}/votar")
    public ResponseEntity<Void> votar(@PathVariable Integer id, @RequestParam Boolean like,
            @RequestParam Integer usuarioId) {
        if (noticiaServicio.votar(id, like, usuarioId)) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * @author amorcia
     *         METODO - Obtiene el tipo de voto de un usuario en una noticia
     * @param id        ID de la noticia
     * @param usuarioId ID del usuario
     * @return ResponseEntity con tipo ("LIKE", "DISLIKE", "NONE")
     */
    @GetMapping("/{id}/voto")
    public ResponseEntity<java.util.Map<String, String>> obtenerVoto(@PathVariable Integer id,
            @RequestParam Integer usuarioId) {
        String tipo = noticiaServicio.obtenerTipoVoto(id, usuarioId);
        return ResponseEntity.ok(java.util.Map.of("tipo", tipo != null ? tipo : "NONE"));
    }

    /**
     * @author amorcia
     *         METODO - Elimina noticia con confirmación estricta de título (para
     *         propietarios)
     * @param id      ID de la noticia
     * @param payload Mapa con título de confirmación y usuarioId
     * @return ResponseEntity con mensaje de éxito o error
     */
    @PostMapping("/{id}/eliminar-con-confirmacion")
    public ResponseEntity<java.util.Map<String, String>> eliminarConConfirmacionTitulo(
            @PathVariable Integer id,
            @RequestBody java.util.Map<String, Object> payload) {
        try {
            String tituloConfirmacion = (String) payload.get("tituloConfirmacion");
            Integer usuarioId = (Integer) payload.get("usuarioId");

            if (tituloConfirmacion == null || tituloConfirmacion.isBlank() || usuarioId == null) {
                return ResponseEntity.badRequest()
                        .body(java.util.Map.of("error", "Título de confirmación y usuarioId son requeridos"));
            }

            if (!noticiaServicio.esPropietario(id, usuarioId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(java.util.Map.of("error", "Solo el propietario puede eliminar esta noticia"));
            }

            boolean eliminado = noticiaServicio.eliminarConConfirmacionTitulo(tituloConfirmacion, id, usuarioId);
            if (eliminado) {
                return ResponseEntity.ok(java.util.Map.of("mensaje", "Noticia eliminada exitosamente"));
            }
            return ResponseEntity.badRequest()
                    .body(java.util.Map.of("error", "El título no coincide o la noticia no existe"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(java.util.Map.of("error", "Error al eliminar noticia: " + e.getMessage()));
        }
    }

    /**
     * @author amorcia
     *         METODO - Elimina noticia con justificación (para staff
     *         administrativo)
     * @param id      ID de la noticia
     * @param payload Mapa con motivo, descripción y eliminadorId
     * @return ResponseEntity con mensaje de éxito o error
     */
    @PostMapping("/{id}/eliminar-con-justificacion")
    public ResponseEntity<java.util.Map<String, String>> eliminarConJustificacion(
            @PathVariable Integer id,
            @RequestBody java.util.Map<String, Object> payload) {
        try {
            String motivo = (String) payload.get("motivo");
            String descripcion = (String) payload.get("descripcion");
            Integer eliminadorId = (Integer) payload.get("eliminadorId");

            if (motivo == null || motivo.isBlank() || eliminadorId == null) {
                return ResponseEntity.badRequest()
                        .body(java.util.Map.of("error", "Motivo y eliminadorId son requeridos"));
            }

            UsuarioEntidad eliminador = usuarioRepositorio.findById(eliminadorId).orElse(null);
            if (eliminador == null) {
                return ResponseEntity.badRequest()
                        .body(java.util.Map.of("error", "Eliminador no encontrado"));
            }

            boolean eliminado = noticiaServicio.eliminarConJustificacion(id, motivo, descripcion, eliminador);
            if (eliminado) {
                return ResponseEntity.ok(java.util.Map.of("mensaje", "Noticia eliminada exitosamente"));
            }
            return ResponseEntity.badRequest()
                    .body(java.util.Map.of("error", "No se pudo eliminar la noticia"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(java.util.Map.of("error", "Error al eliminar noticia: " + e.getMessage()));
        }
    }

    /**
     * @author amorcia
     *         METODO - Método deprecado para eliminar. Usar versiones con
     *         justificación/confirmación.
     * @deprecated
     */
    @Deprecated
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

    /**
     * @author amorcia
     *         METODO - Elimina noticia por título (deprecado/interno)
     */
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

    /**
     * @author amorcia
     *         METODO - Lista las noticias eliminadas (solo admin)
     * @return ResponseEntity con lista de noticias eliminadas
     */
    @GetMapping("/eliminadas/admin")
    public ResponseEntity<List<NoticiaEliminadaEntidad>> listarEliminadasAdmin() {
        return ResponseEntity.ok(noticiaServicio.listarNoticiasEliminadasPorAdmin());
    }

    /**
     * @author amorcia
     *         METODO - Obtiene detalles de una noticia eliminada
     * @param id ID de la noticia eliminada
     * @return ResponseEntity con detalles
     */
    @GetMapping("/eliminadas/{id}")
    public ResponseEntity<NoticiaEliminadaEntidad> obtenerEliminadaPorId(@PathVariable Long id) {
        return noticiaServicio.obtenerNoticiaEliminadaPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * @author amorcia
     *         METODO - Verifica si existe una noticia con un título específico
     * @param titulo Título a verificar
     * @return ResponseEntity con booleano (true si existe)
     */
    @GetMapping("/check-titulo")
    public ResponseEntity<Boolean> verificarTitulo(@RequestParam String titulo) {
        return ResponseEntity.ok(noticiaServicio.existePorTitulo(titulo));
    }
}
