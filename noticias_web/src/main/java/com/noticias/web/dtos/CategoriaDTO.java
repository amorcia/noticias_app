package com.noticias.web.dtos;

import java.util.List;

public class CategoriaDTO {
    private Integer id;
    private String nombre;
    private String descripcion;
    private String color;
    private Integer parentId;
    private List<CategoriaDTO> subcategorias;

    public CategoriaDTO() {
    }

    // Getters and Setters
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

    public Integer getParentId() {
        return parentId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    public List<CategoriaDTO> getSubcategorias() {
        return subcategorias;
    }

    public void setSubcategorias(List<CategoriaDTO> subcategorias) {
        this.subcategorias = subcategorias;
    }
}
