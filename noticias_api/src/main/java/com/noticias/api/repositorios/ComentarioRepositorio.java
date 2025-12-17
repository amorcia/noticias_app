package com.noticias.api.repositorios;

import com.noticias.api.entidades.ComentarioEntidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComentarioRepositorio extends JpaRepository<ComentarioEntidad, Integer> {
    List<ComentarioEntidad> findByNoticiaId(Integer noticiaId);

    List<ComentarioEntidad> findByUsuarioId(Integer usuarioId);
}
