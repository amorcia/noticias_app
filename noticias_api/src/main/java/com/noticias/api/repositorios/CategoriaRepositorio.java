package com.noticias.api.repositorios;

import com.noticias.api.entidades.CategoriaEntidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoriaRepositorio extends JpaRepository<CategoriaEntidad, Integer> {
    List<CategoriaEntidad> findByParentIsNull();

    List<CategoriaEntidad> findByParentId(Integer parentId);

    java.util.Optional<CategoriaEntidad> findByNombre(String nombre);
}
