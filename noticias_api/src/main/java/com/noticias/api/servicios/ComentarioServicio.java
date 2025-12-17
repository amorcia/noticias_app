package com.noticias.api.servicios;

import com.noticias.api.entidades.ComentarioEntidad;
import com.noticias.api.repositorios.ComentarioRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Servicio para gestión de comentarios.
 */
@Service
public class ComentarioServicio {

    private final ComentarioRepositorio comentarioRepositorio;

    public ComentarioServicio(ComentarioRepositorio comentarioRepositorio) {
        this.comentarioRepositorio = comentarioRepositorio;
    }

    public List<ComentarioEntidad> listarPorNoticia(Integer noticiaId) {
        return comentarioRepositorio.findByNoticiaId(noticiaId);
    }

    public List<ComentarioEntidad> listarPorUsuario(Integer usuarioId) {
        return comentarioRepositorio.findByUsuarioId(usuarioId);
    }

    public Optional<ComentarioEntidad> buscarPorId(Integer id) {
        if (id == null)
            return Optional.empty();
        return comentarioRepositorio.findById(id);
    }

    @Transactional
    public ComentarioEntidad crearComentario(ComentarioEntidad comentario) {
        if (comentario.getFecha() == null) {
            comentario.setFecha(LocalDateTime.now());
        }
        return comentarioRepositorio.save(comentario);
    }

    @Transactional
    public boolean eliminarComentario(Integer id) {
        if (id != null && comentarioRepositorio.existsById(id)) {
            comentarioRepositorio.deleteById(id);
            return true;
        }
        return false;
    }
}
