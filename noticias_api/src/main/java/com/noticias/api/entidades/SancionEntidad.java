package com.noticias.api.entidades;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sanciones")
public class SancionEntidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "san_id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usu_id", nullable = false)
    private UsuarioEntidad usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id")
    private UsuarioEntidad admin;

    @Column(name = "san_tipo", nullable = false)
    private String tipo; // "TEMPORAL", "PERMANENTE"

    @Column(name = "san_estado", nullable = false)
    private String estado; // "PENDIENTE", "RESUELTO"

    @Column(name = "san_motivo", columnDefinition = "TEXT")
    private String motivo;

    @Column(name = "san_apelacion", columnDefinition = "TEXT")
    private String apelacion;

    @Column(name = "san_resolucion", columnDefinition = "TEXT")
    private String resolucion;

    @Column(name = "san_fecha_inicio", nullable = false)
    private LocalDateTime fechaInicio;

    @Column(name = "san_fecha_fin")
    private LocalDateTime fechaFin;

    public SancionEntidad() {
    }

    // Getters y Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public UsuarioEntidad getUsuario() {
        return usuario;
    }

    public void setUsuario(UsuarioEntidad usuario) {
        this.usuario = usuario;
    }

    public UsuarioEntidad getAdmin() {
        return admin;
    }

    public void setAdmin(UsuarioEntidad admin) {
        this.admin = admin;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public String getApelacion() {
        return apelacion;
    }

    public void setApelacion(String apelacion) {
        this.apelacion = apelacion;
    }

    public String getResolucion() {
        return resolucion;
    }

    public void setResolucion(String resolucion) {
        this.resolucion = resolucion;
    }

    public LocalDateTime getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDateTime fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDateTime getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDateTime fechaFin) {
        this.fechaFin = fechaFin;
    }
}
