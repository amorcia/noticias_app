package com.noticias.api.servicios;

import com.noticias.api.entidades.CategoriaEntidad;
import com.noticias.api.repositorios.CategoriaRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Servicio para gestión de categorías y subcategorías.
 */
@Service
@SuppressWarnings("null")
public class CategoriaServicio {

    private final CategoriaRepositorio categoriaRepositorio;

    public CategoriaServicio(CategoriaRepositorio categoriaRepositorio) {
        this.categoriaRepositorio = categoriaRepositorio;
    }

    public List<CategoriaEntidad> listarTodas() {
        return categoriaRepositorio.findAll();
    }

    public List<CategoriaEntidad> listarCategoriasRaiz() {
        return categoriaRepositorio.findByParentIsNull();
    }

    public List<CategoriaEntidad> listarSubcategorias(Integer parentId) {
        return categoriaRepositorio.findByParentId(parentId);
    }

    public Optional<CategoriaEntidad> buscarPorId(Integer id) {
        if (id == null)
            return Optional.empty();
        return categoriaRepositorio.findById(id);
    }

    public Optional<CategoriaEntidad> buscarPorNombre(String nombre) {
        return categoriaRepositorio.findByNombre(nombre);
    }

    @Transactional
    public CategoriaEntidad crearCategoria(CategoriaEntidad categoria) {
        if (categoria == null)
            throw new IllegalArgumentException("La categoría no puede ser nula");
        return categoriaRepositorio.save(categoria);
    }

    @Transactional
    public CategoriaEntidad actualizarCategoria(Integer id, CategoriaEntidad categoriaActualizada) {
        if (id == null)
            return null;
        return categoriaRepositorio.findById(id).map(categoria -> {
            if (categoriaActualizada.getNombre() != null) {
                categoria.setNombre(categoriaActualizada.getNombre());
            }
            if (categoriaActualizada.getDescripcion() != null) {
                categoria.setDescripcion(categoriaActualizada.getDescripcion());
            }
            if (categoriaActualizada.getColor() != null) {
                categoria.setColor(categoriaActualizada.getColor());
            }
            return categoriaRepositorio.save(categoria);
        }).orElse(null);
    }

    @Transactional
    public boolean eliminarCategoria(Integer id) {
        if (id != null && categoriaRepositorio.existsById(id)) {
            categoriaRepositorio.deleteById(id);
            return true;
        }
        return false;
    }
}
