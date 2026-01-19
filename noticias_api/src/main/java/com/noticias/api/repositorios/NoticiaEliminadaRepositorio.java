package com.noticias.api.repositorios;

import com.noticias.api.entidades.NoticiaEliminadaEntidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NoticiaEliminadaRepositorio extends JpaRepository<NoticiaEliminadaEntidad, Integer> {
    List<NoticiaEliminadaEntidad> findByRolEliminadorIn(List<String> roles);
}
