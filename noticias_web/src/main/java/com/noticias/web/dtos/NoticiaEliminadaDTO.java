package com.noticias.web.dtos;

import java.time.LocalDateTime;

public class NoticiaEliminadaDTO {
    private Integer id;
    private String titulo;
    private String autorNombre;
    private String categoriaNombre;
    private LocalDateTime fechaEliminacion;
    private String motivo;
    private String eliminadoPor;
    private String rolEliminador;

    public NoticiaEliminadaDTO() {
    }

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
