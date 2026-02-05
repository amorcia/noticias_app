package com.noticias.api.repositorios;

import com.noticias.api.entidades.ComentarioEntidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ComentarioRepositorio extends JpaRepository<ComentarioEntidad, Integer> {
    List<ComentarioEntidad> findByNoticiaIdOrderByFechaDesc(Integer noticiaId);

    List<ComentarioEntidad> findByNoticiaIdAndPadreIsNullOrderByFechaDesc(Integer noticiaId);

    long countByNoticiaId(Integer noticiaId);

    void deleteByAutorId(Integer autorId);
}
