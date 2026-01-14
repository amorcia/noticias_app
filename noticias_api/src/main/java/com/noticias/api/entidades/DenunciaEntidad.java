package com.noticias.api.entidades;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "denuncias")
public class DenunciaEntidad {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private UsuarioEntidad denunciante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "noticia_id", nullable = true)
    private NoticiaEntidad noticia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comentario_id", nullable = true)
    private ComentarioEntidad comentario;

    private String motivo; // Enum logic handled as String for flexibility: "SPAM", "ODIO", "OTRO"
    private String descripcion; // Optional description
    private LocalDateTime fecha;

    @Column(columnDefinition = "VARCHAR(20) DEFAULT 'PENDIENTE'")
    private String estado = "PENDIENTE";

    public DenunciaEntidad() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public UsuarioEntidad getDenunciante() {
        return denunciante;
    }

    public void setDenunciante(UsuarioEntidad denunciante) {
        this.denunciante = denunciante;
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

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
