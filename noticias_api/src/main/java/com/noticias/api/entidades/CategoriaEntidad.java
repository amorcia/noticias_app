package com.noticias.api.entidades;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad Categoria con soporte para subcategorías (auto-referencia).
 */
@Entity
@Table(name = "categorias")
public class CategoriaEntidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 50)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(length = 20)
    private String color;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    @JsonIgnore // Evitar serialización circular
    private CategoriaEntidad parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    // @JsonIgnore removed to allow tree structure in API responses (e.g. for Nav)
    private List<CategoriaEntidad> subcategorias = new ArrayList<>();

    public CategoriaEntidad() {
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

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public CategoriaEntidad getParent() {
        return parent;
    }

    public void setParent(CategoriaEntidad parent) {
        this.parent = parent;
    }

    public List<CategoriaEntidad> getSubcategorias() {
        return subcategorias;
    }

    public void setSubcategorias(List<CategoriaEntidad> subcategorias) {
        this.subcategorias = subcategorias;
    }
}
