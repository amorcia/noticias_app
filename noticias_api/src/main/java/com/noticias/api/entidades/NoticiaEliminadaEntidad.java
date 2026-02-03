package com.noticias.api.entidades;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad para almacenar noticias eliminadas con todos sus datos
 */
@Entity
@Table(name = "noticias_eliminadas")
public class NoticiaEliminadaEntidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "nel_id")
    private Long id;

    // Datos completos de la noticia original
    @Column(name = "nel_titulo", nullable = false, length = 255)
    private String titulo;

    @Column(name = "nel_subtitulo", length = 255)
    private String subtitulo;

    @Column(name = "nel_contenido", nullable = false, columnDefinition = "TEXT")
    private String contenido;

    @Column(name = "nel_imagen_url", columnDefinition = "TEXT")
    private String imagenUrl;

    // Información del autor original
    @Column(name = "nel_autor_original_id")
    private Integer autorOriginalId;

    @Column(name = "nel_autor_original_nombre")
    private String autorOriginalNombre;

    // Información de categoría
    @Column(name = "nel_categoria_id")
    private Integer categoriaId;

    @Column(name = "nel_categoria_nombre")
    private String categoriaNombre;

    @Column(name = "nel_categoria_color")
    private String categoriaColor;

    // Estadísticas de la noticia
    @Column(name = "nel_likes")
    private Integer likes = 0;
    @Column(name = "nel_dislikes")
    private Integer dislikes = 0;
    @Column(name = "nel_visitas")
    private Integer visitas = 0;
    @Column(name = "nel_comentarios_count")
    private Integer comentariosCount = 0;

    @Column(name = "nel_fecha_publicacion_original")
    private LocalDateTime fechaPublicacionOriginal;

    // Información de eliminación
    @Column(name = "nel_motivo", nullable = false)
    private String motivo;

    @Column(name = "nel_descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "nel_eliminado_por_id")
    private Integer eliminadoPorId;

    @Column(name = "nel_eliminado_por_nombre")
    private String eliminadoPorNombre;

    @Column(name = "nel_rol_eliminador")
    private String rolEliminador;

    @Column(name = "nel_fecha_eliminacion")
    private LocalDateTime fechaEliminacion;

    @Column(name = "nel_es_aportacion_usuario")
    private Boolean esAportacionUsuario = false;

    public NoticiaEliminadaEntidad() {
    }

    // Constructor completo
    public NoticiaEliminadaEntidad(NoticiaEntidad noticia, String motivo, String descripcion,
            UsuarioEntidad eliminador) {
        // Datos de la noticia
        this.titulo = noticia.getTitulo();
        this.subtitulo = noticia.getSubtitulo();
        this.contenido = noticia.getContenido();
        this.imagenUrl = noticia.getImagenUrl();

        // Autor original
        if (noticia.getAutor() != null) {
            this.autorOriginalId = noticia.getAutor().getId();
            this.autorOriginalNombre = noticia.getAutor().getNombreCompleto();
        }

        // Categoría
        if (noticia.getCategoria() != null) {
            this.categoriaId = noticia.getCategoria().getId();
            this.categoriaNombre = noticia.getCategoria().getNombre();
            this.categoriaColor = noticia.getCategoria().getColor();
        }

        // Estadísticas
        this.likes = noticia.getLikes();
        this.dislikes = noticia.getDislikes();
        this.visitas = noticia.getVisitas();
        this.comentariosCount = noticia.getComentariosCount();
        this.fechaPublicacionOriginal = noticia.getFechaPublicacion();
        this.esAportacionUsuario = noticia.getEsAportacionUsuario();

        // Información de eliminación
        this.motivo = motivo;
        this.descripcion = descripcion;
        this.fechaEliminacion = LocalDateTime.now();

        if (eliminador != null) {
            this.eliminadoPorId = eliminador.getId();
            this.eliminadoPorNombre = eliminador.getNombreCompleto();
            this.rolEliminador = eliminador.getRol() != null ? eliminador.getRol().getNombre() : "UNKNOWN";
        }
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getSubtitulo() {
        return subtitulo;
    }

    public void setSubtitulo(String subtitulo) {
        this.subtitulo = subtitulo;
    }

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    public String getImagenUrl() {
        return imagenUrl;
    }

    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
    }

    public Integer getAutorOriginalId() {
        return autorOriginalId;
    }

    public void setAutorOriginalId(Integer autorOriginalId) {
        this.autorOriginalId = autorOriginalId;
    }

    public String getAutorOriginalNombre() {
        return autorOriginalNombre;
    }

    public void setAutorOriginalNombre(String autorOriginalNombre) {
        this.autorOriginalNombre = autorOriginalNombre;
    }

    public Integer getCategoriaId() {
        return categoriaId;
    }

    public void setCategoriaId(Integer categoriaId) {
        this.categoriaId = categoriaId;
    }

    public String getCategoriaNombre() {
        return categoriaNombre;
    }

    public void setCategoriaNombre(String categoriaNombre) {
        this.categoriaNombre = categoriaNombre;
    }

    public String getCategoriaColor() {
        return categoriaColor;
    }

    public void setCategoriaColor(String categoriaColor) {
        this.categoriaColor = categoriaColor;
    }

    public Integer getLikes() {
        return likes;
    }

    public void setLikes(Integer likes) {
        this.likes = likes;
    }

    public Integer getDislikes() {
        return dislikes;
    }

    public void setDislikes(Integer dislikes) {
        this.dislikes = dislikes;
    }

    public Integer getVisitas() {
        return visitas;
    }

    public void setVisitas(Integer visitas) {
        this.visitas = visitas;
    }

    public Integer getComentariosCount() {
        return comentariosCount;
    }

    public void setComentariosCount(Integer comentariosCount) {
        this.comentariosCount = comentariosCount;
    }

    public LocalDateTime getFechaPublicacionOriginal() {
        return fechaPublicacionOriginal;
    }

    public void setFechaPublicacionOriginal(LocalDateTime fechaPublicacionOriginal) {
        this.fechaPublicacionOriginal = fechaPublicacionOriginal;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Integer getEliminadoPorId() {
        return eliminadoPorId;
    }

    public void setEliminadoPorId(Integer eliminadoPorId) {
        this.eliminadoPorId = eliminadoPorId;
    }

    public String getEliminadoPorNombre() {
        return eliminadoPorNombre;
    }

    public void setEliminadoPorNombre(String eliminadoPorNombre) {
        this.eliminadoPorNombre = eliminadoPorNombre;
    }

    public String getRolEliminador() {
        return rolEliminador;
    }

    public void setRolEliminador(String rolEliminador) {
        this.rolEliminador = rolEliminador;
    }

    public LocalDateTime getFechaEliminacion() {
        return fechaEliminacion;
    }

    public void setFechaEliminacion(LocalDateTime fechaEliminacion) {
        this.fechaEliminacion = fechaEliminacion;
    }

    public Boolean getEsAportacionUsuario() {
        return esAportacionUsuario;
    }

    public void setEsAportacionUsuario(Boolean esAportacionUsuario) {
        this.esAportacionUsuario = esAportacionUsuario;
    }
}
