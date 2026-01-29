package com.noticias.web.dtos;

import java.time.LocalDateTime;

public class ComentarioDTO {
    private Integer id;
    private String contenido;
    private AutorDTO autor;
    private Integer noticiaId;
    private LocalDateTime fecha;
    private Integer likes = 0;
    private Integer dislikes = 0;
    private Integer padreId;
    private String votoUsuario = "NONE";
    private java.util.List<ComentarioDTO> respuestas = new java.util.ArrayList<>();

    public String getVotoUsuario() {
        return votoUsuario;
    }

    public void setVotoUsuario(String votoUsuario) {
        this.votoUsuario = votoUsuario;
    }

    public ComentarioDTO() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    public AutorDTO getAutor() {
        return autor;
    }

    public void setAutor(AutorDTO autor) {
        this.autor = autor;
    }

    // Convenience getters for Thymeleaf
    public Integer getUsuarioId() {
        return autor != null ? autor.getId() : null;
    }

    public String getUsuarioNombre() {
        return autor != null ? autor.getNombreCompleto() : "Anónimo";
    }

    public Integer getNoticiaId() {
        return noticiaId;
    }

    public void setNoticiaId(Integer noticiaId) {
        this.noticiaId = noticiaId;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
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

    public Integer getPadreId() {
        return padreId;
    }

    public void setPadreId(Integer padreId) {
        this.padreId = padreId;
    }

    public java.util.List<ComentarioDTO> getRespuestas() {
        return respuestas;
    }

    public void setRespuestas(java.util.List<ComentarioDTO> respuestas) {
        this.respuestas = respuestas;
    }

    public static class AutorDTO {
        private Integer id;
        private String nombreCompleto;
        private String imagenUrl;

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getNombreCompleto() {
            return nombreCompleto;
        }

        public void setNombreCompleto(String nombreCompleto) {
            this.nombreCompleto = nombreCompleto;
        }

        public String getImagenUrl() {
            return imagenUrl;
        }

        public void setImagenUrl(String imagenUrl) {
            this.imagenUrl = imagenUrl;
        }
    }

    // Convenience getter for the author's image URL
    public String getUsuarioImagenUrl() {
        return autor != null ? autor.getImagenUrl() : null;
    }
}
