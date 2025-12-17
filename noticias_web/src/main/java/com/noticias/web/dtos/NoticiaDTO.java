package com.noticias.web.dtos;

import java.time.LocalDateTime;

public class NoticiaDTO {
    private Integer id;
    private String titulo;
    private String subtitulo;
    private String contenido;
    private String imagenUrl;
    private Integer autorId;
    private String autorNombre;
    private Integer categoriaId;
    private String categoriaNombre;
    private String categoriaColor;
    private LocalDateTime fechaPublicacion;
    private Integer visitas;
    private Boolean destacada;
    private Boolean esAportacionUsuario;
    private Integer likes;
    private Integer dislikes;
    private Integer comentariosCount;

    public NoticiaDTO() {
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

    public Integer getAutorId() {
        return autorId;
    }

    public void setAutorId(Integer autorId) {
        this.autorId = autorId;
    }

    public String getAutorNombre() {
        return autorNombre;
    }

    public void setAutorNombre(String autorNombre) {
        this.autorNombre = autorNombre;
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

    public LocalDateTime getFechaPublicacion() {
        return fechaPublicacion;
    }

    public void setFechaPublicacion(LocalDateTime fechaPublicacion) {
        this.fechaPublicacion = fechaPublicacion;
    }

    public Integer getVisitas() {
        return visitas;
    }

    public void setVisitas(Integer visitas) {
        this.visitas = visitas;
    }

    public Boolean getDestacada() {
        return destacada;
    }

    public void setDestacada(Boolean destacada) {
        this.destacada = destacada;
    }

    public Boolean getEsAportacionUsuario() {
        return esAportacionUsuario;
    }

    public void setEsAportacionUsuario(Boolean esAportacionUsuario) {
        this.esAportacionUsuario = esAportacionUsuario;
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

    private String autorImagenUrl;

    public String getAutorImagenUrl() {
        return autorImagenUrl;
    }

    public void setAutorImagenUrl(String autorImagenUrl) {
        this.autorImagenUrl = autorImagenUrl;
    }

    public Integer getComentariosCount() {
        return comentariosCount;
    }

    public void setComentariosCount(Integer comentariosCount) {
        this.comentariosCount = comentariosCount;
    }
}
