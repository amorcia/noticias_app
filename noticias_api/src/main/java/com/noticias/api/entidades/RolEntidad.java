package com.noticias.api.entidades;

import jakarta.persistence.*;

/**
 * Entidad simple para roles.
 */
@Entity
@Table(name = "roles")
public class RolEntidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rol_id")
    private Integer id;

    @Column(name = "rol_nombre", nullable = false, unique = true)
    private String nombre; // ADMIN, OWNER, USER, etc.

    @Column(name = "rol_descripcion")
    private String descripcion;

    public RolEntidad() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}
