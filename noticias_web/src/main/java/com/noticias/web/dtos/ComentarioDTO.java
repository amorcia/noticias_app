package com.noticias.web.dtos;

import java.time.LocalDateTime;

public class ComentarioDTO {
    private Integer id;
    private String contenido;
    private AutorDTO autor;
    private Integer noticiaId;
    private LocalDateTime fecha;

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

    public static class AutorDTO {
        private Integer id;
        private String nombreCompleto;

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
    }
}
