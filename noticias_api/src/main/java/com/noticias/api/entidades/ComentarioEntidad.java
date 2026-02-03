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
    @Column(name = "com_id")
    private Integer id;

    @com.fasterxml.jackson.annotation.JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "not_id", nullable = false)
    private NoticiaEntidad noticia;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "usu_id", nullable = false)
    private UsuarioEntidad autor;

    @Column(name = "com_contenido", nullable = false, columnDefinition = "TEXT")
    private String contenido;

    @Column(name = "com_likes", nullable = false)
    private Integer likes = 0;

    @Column(name = "com_dislikes", nullable = false)
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
    @JoinColumn(name = "com_padre_id")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private ComentarioEntidad padre;

    @OneToMany(mappedBy = "padre", cascade = CascadeType.ALL)
    private java.util.List<ComentarioEntidad> respuestas = new java.util.ArrayList<>();

    @Column(name = "com_fecha")
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
