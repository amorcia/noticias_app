package com.noticias.api.entidades;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad Comentario.
 */
@Entity
@Table(name = "comentarios")
public class ComentarioEntidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String contenido;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private UsuarioEntidad usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "noticia_id")
    private NoticiaEntidad noticia;

    @Column(nullable = false)
    private LocalDateTime fecha;

    public ComentarioEntidad() {
    }

    // Getters y setters
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

    public UsuarioEntidad getUsuario() {
        return usuario;
    }

    public void setUsuario(UsuarioEntidad usuario) {
        this.usuario = usuario;
    }

    public NoticiaEntidad getNoticia() {
        return noticia;
    }

    public void setNoticia(NoticiaEntidad noticia) {
        this.noticia = noticia;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }
}
