package com.noticias.api.entidades;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "noticias_eliminadas")
public class NoticiaEliminadaEntidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private Integer noticiaOriginalId;

    @Column(nullable = false, length = 255)
    private String titulo;

    @Column(length = 255)
    private String subtitulo;

    @Column(columnDefinition = "TEXT")
    private String contenido;

    @Column(name = "imagen_url", length = 255)
    private String imagenUrl;

    @Column(nullable = false)
    private String autorNombre;

    @Column(nullable = false)
    private String categoriaNombre;

    @Column(name = "fecha_publicacion")
    private LocalDateTime fechaPublicacion;

    @Column(name = "fecha_eliminacion", nullable = false)
    private LocalDateTime fechaEliminacion;

    @Column(nullable = false)
    private String motivo;

    @Column(name = "eliminado_por", nullable = false)
    private String eliminadoPor; // Nombre/Email del usuario que eliminó

    @Column(nullable = false)
    private String rolEliminador; // ADMIN, OWNER, TRABAJADOR

    public NoticiaEliminadaEntidad() {
    }

    // Constructor helper
    public NoticiaEliminadaEntidad(NoticiaEntidad noticia, String motivo, String eliminadoPor, String rolEliminador) {
        this.noticiaOriginalId = noticia.getId();
        this.titulo = noticia.getTitulo();
        this.subtitulo = noticia.getSubtitulo();
        this.contenido = noticia.getContenido();
        this.imagenUrl = noticia.getImagenUrl();
        this.autorNombre = noticia.getAutor() != null ? noticia.getAutor().getNombreCompleto() : "Desconocido";
        this.categoriaNombre = noticia.getCategoria() != null ? noticia.getCategoria().getNombre() : "Sin Categoria";
        this.fechaPublicacion = noticia.getFechaPublicacion();
        this.fechaEliminacion = LocalDateTime.now();
        this.motivo = motivo;
        this.eliminadoPor = eliminadoPor;
        this.rolEliminador = rolEliminador;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getNoticiaOriginalId() {
        return noticiaOriginalId;
    }

    public void setNoticiaOriginalId(Integer noticiaOriginalId) {
        this.noticiaOriginalId = noticiaOriginalId;
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

    public String getAutorNombre() {
        return autorNombre;
    }

    public void setAutorNombre(String autorNombre) {
        this.autorNombre = autorNombre;
    }

    public String getCategoriaNombre() {
        return categoriaNombre;
    }

    public void setCategoriaNombre(String categoriaNombre) {
        this.categoriaNombre = categoriaNombre;
    }

    public LocalDateTime getFechaPublicacion() {
        return fechaPublicacion;
    }

    public void setFechaPublicacion(LocalDateTime fechaPublicacion) {
        this.fechaPublicacion = fechaPublicacion;
    }

    public LocalDateTime getFechaEliminacion() {
        return fechaEliminacion;
    }

    public void setFechaEliminacion(LocalDateTime fechaEliminacion) {
        this.fechaEliminacion = fechaEliminacion;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public String getEliminadoPor() {
        return eliminadoPor;
    }

    public void setEliminadoPor(String eliminadoPor) {
        this.eliminadoPor = eliminadoPor;
    }

    public String getRolEliminador() {
        return rolEliminador;
    }

    public void setRolEliminador(String rolEliminador) {
        this.rolEliminador = rolEliminador;
    }
}
