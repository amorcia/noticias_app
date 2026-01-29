package com.noticias.api.entidades;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "comentarios")
public class ComentarioEntidad {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @com.fasterxml.jackson.annotation.JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "noticia_id", nullable = false)
    private NoticiaEntidad noticia;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "usuario_id", nullable = false)
    private UsuarioEntidad autor;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String contenido;

    @Column(nullable = false)
    private Integer likes = 0;

    @Column(nullable = false)
    private Integer dislikes = 0;

    @jakarta.persistence.Transient
    private String votoUsuario = "NONE";

    public String getVotoUsuario() {
        return votoUsuario;
    }

    public void setVotoUsuario(String votoUsuario) {
        this.votoUsuario = votoUsuario;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "padre_id")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private ComentarioEntidad padre;

    @OneToMany(mappedBy = "padre", cascade = CascadeType.ALL)
    private java.util.List<ComentarioEntidad> respuestas = new java.util.ArrayList<>();

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

    public ComentarioEntidad getPadre() {
        return padre;
    }

    public void setPadre(ComentarioEntidad padre) {
        this.padre = padre;
    }

    public java.util.List<ComentarioEntidad> getRespuestas() {
        return respuestas;
    }

    public void setRespuestas(java.util.List<ComentarioEntidad> respuestas) {
        this.respuestas = respuestas;
    }
}
