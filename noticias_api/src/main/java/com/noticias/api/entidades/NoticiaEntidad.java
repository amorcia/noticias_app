package com.noticias.api.entidades;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad Noticia.
 */
@Entity
@Table(name = "noticias")
public class NoticiaEntidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "not_id")
    private Integer id;

    @Column(name = "not_titulo", nullable = false, length = 255)
    private String titulo;

    @Column(name = "not_subtitulo", length = 255)
    private String subtitulo;

    @Column(name = "not_contenido", nullable = false, columnDefinition = "TEXT")
    private String contenido;

    @Column(name = "not_imagen_url", columnDefinition = "TEXT", nullable = true)
    private String imagenUrl;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "usu_id")
    private UsuarioEntidad autor;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cat_id")
    private CategoriaEntidad categoria;

    @Column(name = "not_fecha_publicacion")
    private LocalDateTime fechaPublicacion;

    @Column(name = "not_es_aportacion_usuario", nullable = false, columnDefinition = "boolean default false")
    private Boolean esAportacionUsuario = false;

    @Column(name = "not_likes", nullable = false)
    private Integer likes = 0;

    @Column(name = "not_dislikes", nullable = false)
    private Integer dislikes = 0;

    @Column(name = "not_comentarios_count", nullable = false)
    private Integer comentariosCount = 0;

    @Transient
    public Double getPopularidad() {
        return (double) (likes - dislikes) + (comentariosCount * 0.5);
    }

    @Column(name = "not_visitas", nullable = false)
    private Integer visitas = 0;

    @Column(name = "not_destacada", nullable = false)
    private Boolean destacada = false;

    public NoticiaEntidad() {
    }

    // Getters y setters
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

    public String getSubtitulo() {
        return subtitulo;
    }

    public void setSubtitulo(String subtitulo) {
        this.subtitulo = subtitulo;
    }

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    public String getImagenUrl() {
        return imagenUrl;
    }

    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
    }

    public UsuarioEntidad getAutor() {
        return autor;
    }

    public void setAutor(UsuarioEntidad autor) {
        this.autor = autor;
    }

    public CategoriaEntidad getCategoria() {
        return categoria;
    }

    public void setCategoria(CategoriaEntidad categoria) {
        this.categoria = categoria;
    }

    public LocalDateTime getFechaPublicacion() {
        return fechaPublicacion;
    }

    public void setFechaPublicacion(LocalDateTime fechaPublicacion) {
        this.fechaPublicacion = fechaPublicacion;
    }

    public Integer getVisitas() {
        return visitas;
    }

    public void setVisitas(Integer visitas) {
        this.visitas = visitas;
    }

    public Boolean getDestacada() {
        return destacada;
    }

    public void setDestacada(Boolean destacada) {
        this.destacada = destacada;
    }

    public Boolean getEsAportacionUsuario() {
        return esAportacionUsuario;
    }

    public void setEsAportacionUsuario(Boolean esAportacionUsuario) {
        this.esAportacionUsuario = esAportacionUsuario;
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

    public Integer getComentariosCount() {
        return comentariosCount;
    }

    public void setComentariosCount(Integer comentariosCount) {
        this.comentariosCount = comentariosCount;
    }
}
