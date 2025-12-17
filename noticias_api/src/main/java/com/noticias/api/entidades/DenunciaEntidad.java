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
    @JoinColumn(name = "autor_id", nullable = false)
    private UsuarioEntidad autor;

    @Column(nullable = false)
    private String tipo; // "NOTICIA", "COMENTARIO"

    @Column(name = "id_objeto", nullable = false)
    private Integer idObjeto;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String motivo;

    @Column(nullable = false)
    private String estado; // "PENDIENTE", "RESUELTO", "DESCARTADA"

    @Column(nullable = false)
    private LocalDateTime fecha;

    public DenunciaEntidad() {
    }

    // Getters y Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public UsuarioEntidad getAutor() {
        return autor;
    }

    public void setAutor(UsuarioEntidad autor) {
        this.autor = autor;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public Integer getIdObjeto() {
        return idObjeto;
    }

    public void setIdObjeto(Integer idObjeto) {
        this.idObjeto = idObjeto;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
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
