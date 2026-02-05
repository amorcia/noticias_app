package com.noticias.api.repositorios;

import com.noticias.api.entidades.NoticiaEntidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NoticiaRepositorio extends JpaRepository<NoticiaEntidad, Integer> {
    List<NoticiaEntidad> findByDestacadaTrue();

    List<NoticiaEntidad> findByCategoriaId(Integer categoriaId);

    List<NoticiaEntidad> findByAutorId(Integer autorId);

    @Query("SELECT n FROM NoticiaEntidad n WHERE n.categoria.nombre LIKE %:nombre% OR n.titulo LIKE %:nombre%")
    List<NoticiaEntidad> buscarPorParteDelTituloOCategoria(@Param("nombre") String nombre);

    boolean existsByTitulo(String titulo);

    Optional<NoticiaEntidad> findByTitulo(String titulo);

    @Query("SELECT n FROM NoticiaEntidad n WHERE " +
            "REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(LOWER(n.titulo), 'á', 'a'), 'é', 'e'), 'í', 'i'), 'ó', 'o'), 'ú', 'u'), 'ñ', 'n') = "
            +
            "REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(LOWER(:titulo), 'á', 'a'), 'é', 'e'), 'í', 'i'), 'ó', 'o'), 'ú', 'u'), 'ñ', 'n')")
    Optional<NoticiaEntidad> findByTituloIgnoreCase(String titulo);

    @Query("SELECT n FROM NoticiaEntidad n JOIN n.categoria c WHERE c.nombre = :nombreCategoria")
    List<NoticiaEntidad> findByCategoriaNombre(String nombreCategoria);

    @Query("SELECT n FROM NoticiaEntidad n JOIN n.categoria c WHERE c.nombre = :nombreCategoria AND n.esAportacionUsuario = :esAportacion")
    List<NoticiaEntidad> findByCategoriaNombreAndEsAportacionUsuario(String nombreCategoria, Boolean esAportacion);

    @Query("SELECT n FROM NoticiaEntidad n ORDER BY (COALESCE(n.likes, 0) + COALESCE(n.comentariosCount, 0) - COALESCE(n.dislikes, 0)) DESC")
    List<NoticiaEntidad> findTopPopular(org.springframework.data.domain.Pageable pageable);

    // Filtros
    List<NoticiaEntidad> findByCategoriaIdOrderByFechaPublicacionDesc(Integer categoriaId);

    @Query("SELECT n FROM NoticiaEntidad n WHERE n.categoria.id = :catId AND MONTH(n.fechaPublicacion) = :mes AND YEAR(n.fechaPublicacion) = :anio")
    List<NoticiaEntidad> findByCategoriaIdAndMesAndAnio(Integer catId, int mes, int anio);

    @Query("SELECT n FROM NoticiaEntidad n WHERE n.categoria.id = :catId ORDER BY (n.likes - n.dislikes) DESC")
    List<NoticiaEntidad> findByCategoriaIdOrdenPorValoracionDesc(Integer catId);

    @Query("SELECT n FROM NoticiaEntidad n WHERE n.categoria.id = :catId ORDER BY (n.likes - n.dislikes) ASC")
    List<NoticiaEntidad> findByCategoriaIdOrdenPorValoracionAsc(Integer catId);

    void deleteByAutorId(Integer autorId);
}
