package com.noticias.api.entidades;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad para almacenar usuarios eliminados con todos sus datos
 */
@Entity
@Table(name = "usuarios_eliminados")
public class UsuarioEliminadoEntidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "uel_id")
    private Long id;

    // Datos del usuario original
    @Column(name = "uel_nombre_completo")
    private String nombreCompleto;

    @Column(name = "uel_email")
    private String email;

    @Column(name = "uel_rol_nombre")
    private String rolNombre;

    // Información de eliminación
    @Column(name = "uel_motivo", nullable = false)
    private String motivo;

    @Column(name = "uel_descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "uel_eliminado_por_id")
    private Integer eliminadoPorId;

    @Column(name = "uel_eliminado_por_nombre")
    private String eliminadoPorNombre;

    @Column(name = "uel_fecha_eliminacion")
    private LocalDateTime fechaEliminacion;

    public UsuarioEliminadoEntidad() {
    }

    // Constructor completo
    public UsuarioEliminadoEntidad(UsuarioEntidad usuario, String motivo, String descripcion,
            UsuarioEntidad eliminador) {
        this.nombreCompleto = usuario.getNombreCompleto();
        this.email = usuario.getEmail();
        this.rolNombre = usuario.getRol() != null ? usuario.getRol().getNombre() : "UNKNOWN";
        this.motivo = motivo;
        this.descripcion = descripcion;
        this.fechaEliminacion = LocalDateTime.now();

        if (eliminador != null) {
            this.eliminadoPorId = eliminador.getId();
            this.eliminadoPorNombre = eliminador.getNombreCompleto();
        }
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRolNombre() {
        return rolNombre;
    }

    public void setRolNombre(String rolNombre) {
        this.rolNombre = rolNombre;
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

    public Integer getEliminadoPorId() {
        return eliminadoPorId;
    }

    public void setEliminadoPorId(Integer eliminadoPorId) {
        this.eliminadoPorId = eliminadoPorId;
    }

    public String getEliminadoPorNombre() {
        return eliminadoPorNombre;
    }

    public void setEliminadoPorNombre(String eliminadoPorNombre) {
        this.eliminadoPorNombre = eliminadoPorNombre;
    }

    public LocalDateTime getFechaEliminacion() {
        return fechaEliminacion;
    }

    public void setFechaEliminacion(LocalDateTime fechaEliminacion) {
        this.fechaEliminacion = fechaEliminacion;
    }
}
