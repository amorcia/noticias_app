package com.noticias.api.entidades;

import jakarta.persistence.*;

/**
 * Entidad Etiqueta.
 */
@Entity
@Table(name = "etiquetas")
public class EtiquetaEntidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 50)
    private String nombre;

    public EtiquetaEntidad() {
    }

    // Getters y setters
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
}
