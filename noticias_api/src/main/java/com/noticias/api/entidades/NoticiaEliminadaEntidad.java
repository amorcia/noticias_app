package com.noticias.api.entidades;

import jakarta.persistence.*;

@Entity
@Table(name = "noticias_eliminadas")
public class NoticiaEliminadaEntidad {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "noticia_id", nullable = true) // Guardamos referencia si queremos, o solo datos planos
    private NoticiaEntidad noticiaOriginal;

    // Better to store flat data in case Noticia is hard deleted?
    // Usually "eliminadas" implies archiving.
    // The previous code passed 'NoticiaEntidad' to constructor.

    private String titulo;
    private String motivo;
    private String eliminadoPor; // Email
    private String rolEliminador;

    public NoticiaEliminadaEntidad() {
    }

    public NoticiaEliminadaEntidad(NoticiaEntidad noticia, String motivo, String eliminadoPor, String rolEliminador) {
        this.titulo = noticia.getTitulo();
        this.motivo = motivo;
        this.eliminadoPor = eliminadoPor;
        this.rolEliminador = rolEliminador;
    }

    // Getters Setters
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
