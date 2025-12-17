package com.noticias.web.dtos;

import java.time.LocalDateTime;

public class ComentarioDTO {
    private Integer id;
    private String contenido;
    private Integer usuarioId;
    private String usuarioNombre;
    private Integer noticiaId;
    private LocalDateTime fecha;

    public ComentarioDTO() {
    }

    // Getters and Setters
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

    public Integer getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Integer usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getUsuarioNombre() {
        return usuarioNombre;
    }

    public void setUsuarioNombre(String usuarioNombre) {
        this.usuarioNombre = usuarioNombre;
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
}
