package com.noticias.api.repositorios;

import com.noticias.api.entidades.ComentarioEntidad;
import com.noticias.api.entidades.UsuarioEntidad;
import com.noticias.api.entidades.VotoComentarioEntidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VotoComentarioRepositorio extends JpaRepository<VotoComentarioEntidad, Integer> {
    Optional<VotoComentarioEntidad> findByUsuarioAndComentario(UsuarioEntidad usuario, ComentarioEntidad comentario);
}
