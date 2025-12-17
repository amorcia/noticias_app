package com.noticias.api.repositorios;

import com.noticias.api.entidades.NoticiaEntidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoticiaRepositorio extends JpaRepository<NoticiaEntidad, Integer> {
    List<NoticiaEntidad> findByDestacadaTrue();

    List<NoticiaEntidad> findByCategoriaId(Integer categoriaId);

    List<NoticiaEntidad> findByAutorId(Integer autorId);

    @Query("SELECT n FROM NoticiaEntidad n JOIN n.categoria c WHERE c.nombre = :nombreCategoria")
    List<NoticiaEntidad> findByCategoriaNombre(String nombreCategoria);

    @Query("SELECT n FROM NoticiaEntidad n JOIN n.categoria c WHERE c.nombre = :nombreCategoria AND n.esAportacionUsuario = :esAportacion")
    List<NoticiaEntidad> findByCategoriaNombreAndEsAportacionUsuario(String nombreCategoria, Boolean esAportacion);

    @Query("SELECT n FROM NoticiaEntidad n ORDER BY (COALESCE(n.likes, 0) + COALESCE(n.comentariosCount, 0) - COALESCE(n.dislikes, 0)) DESC")
    List<NoticiaEntidad> findTopPopular(org.springframework.data.domain.Pageable pageable);
}
