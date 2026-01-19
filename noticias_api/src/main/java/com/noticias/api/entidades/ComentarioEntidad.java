package com.noticias.api.entidades;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "comentarios")
public class ComentarioEntidad {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "noticia_id", nullable = false)
    private NoticiaEntidad noticia;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "autor_id", nullable = false)
    private UsuarioEntidad autor;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String contenido;

    private LocalDateTime fecha = LocalDateTime.now();

    public ComentarioEntidad() {
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

    public UsuarioEntidad getAutor() {
        return autor;
    }

    public void setAutor(UsuarioEntidad autor) {
        this.autor = autor;
    }

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }
}
