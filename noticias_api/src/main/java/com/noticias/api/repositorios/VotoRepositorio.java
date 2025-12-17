package com.noticias.api.repositorios;

import com.noticias.api.entidades.NoticiaEntidad;
import com.noticias.api.entidades.UsuarioEntidad;
import com.noticias.api.entidades.VotoEntidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VotoRepositorio extends JpaRepository<VotoEntidad, Integer> {
    Optional<VotoEntidad> findByUsuarioAndNoticia(UsuarioEntidad usuario, NoticiaEntidad noticia);

    long countByNoticiaAndTipo(NoticiaEntidad noticia, String tipo);
}
