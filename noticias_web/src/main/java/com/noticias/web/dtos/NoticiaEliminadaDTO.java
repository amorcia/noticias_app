package com.noticias.web.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

public class NoticiaEliminadaDTO {
    private Integer id;
    private String titulo;
    private String subtitulo;
    private String contenido;
    private String imagenUrl;
    private String autorOriginalNombre;
    private String categoriaNombre;
    private String categoriaColor;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime fechaEliminacion;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime fechaPublicacion;
    private String motivo;
    private String descripcion;
    private String eliminadoPorNombre;
    private String rolEliminador;
    private Integer likes;
    private Integer dislikes;
    private Integer visitas;
    private Integer comentariosCount;
    private Boolean esAportacionUsuario;

    public NoticiaEliminadaDTO() {
    }

    // Getters and Setters

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
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

    public String getAutorOriginalNombre() {
        return autorOriginalNombre;
    }

    public void setAutorOriginalNombre(String autorOriginalNombre) {
        this.autorOriginalNombre = autorOriginalNombre;
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

    public LocalDateTime getFechaEliminacion() {
        return fechaEliminacion;
    }

    public void setFechaEliminacion(LocalDateTime fechaEliminacion) {
        this.fechaEliminacion = fechaEliminacion;
    }

    public LocalDateTime getFechaPublicacion() {
        return fechaPublicacion;
    }

    public void setFechaPublicacion(LocalDateTime fechaPublicacion) {
        this.fechaPublicacion = fechaPublicacion;
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

    public Boolean getEsAportacionUsuario() {
        return esAportacionUsuario;
    }

    public void setEsAportacionUsuario(Boolean esAportacionUsuario) {
        this.esAportacionUsuario = esAportacionUsuario;
    }
}
