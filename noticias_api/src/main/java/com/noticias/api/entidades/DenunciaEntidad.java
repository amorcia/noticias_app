package com.noticias.api.entidades;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "denuncias")
public class DenunciaEntidad {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "noticia_id", nullable = true)
    private NoticiaEntidad noticia;

    @ManyToOne
    @JoinColumn(name = "comentario_id", nullable = true)
    private ComentarioEntidad comentario;

    @ManyToOne
    @JoinColumn(name = "denunciante_id", nullable = false)
    private UsuarioEntidad denunciante;

    @Column(nullable = false)
    private String motivo;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(nullable = false)
    private String estado = "PENDIENTE";

    private LocalDateTime fecha = LocalDateTime.now();

    public DenunciaEntidad() {
    }

    // Getters y Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public NoticiaEntidad getNoticia() {
        return noticia;
    }

    public void setNoticia(NoticiaEntidad noticia) {
        this.noticia = noticia;
    }

    public ComentarioEntidad getComentario() {
        return comentario;
    }

    public void setComentario(ComentarioEntidad comentario) {
        this.comentario = comentario;
    }

    public UsuarioEntidad getDenunciante() {
        return denunciante;
    }

    public void setDenunciante(UsuarioEntidad denunciante) {
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
