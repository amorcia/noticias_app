package com.noticias.web.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

public class DenunciaDTO {
    private Integer id;
    private NoticiaDTO noticia;
    private UsuarioDTO denunciante;
    private String motivo;
    private String descripcion;
    private String estado;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime fecha;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public NoticiaDTO getNoticia() {
        return noticia;
    }

    public void setNoticia(NoticiaDTO noticia) {
        this.noticia = noticia;
    }

    public UsuarioDTO getDenunciante() {
        return denunciante;
    }

    public void setDenunciante(UsuarioDTO denunciante) {
        this.denunciante = denunciante;
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

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }
}
