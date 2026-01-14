package com.noticias.api.servicios;

import com.noticias.api.entidades.ComentarioEntidad;
import com.noticias.api.entidades.NoticiaEntidad;
import com.noticias.api.entidades.UsuarioEntidad;
import com.noticias.api.repositorios.ComentarioRepositorio;
import com.noticias.api.repositorios.NoticiaRepositorio;
import com.noticias.api.repositorios.UsuarioRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ComentarioServicio {

    @Autowired
    private ComentarioRepositorio comentarioRepositorio;
    @Autowired
    private NoticiaRepositorio noticiaRepositorio;
    @Autowired
    private UsuarioRepositorio usuarioRepositorio;

    public ComentarioEntidad crearComentario(Integer noticiaId, Integer usuarioId, String contenido) {
        NoticiaEntidad noticia = noticiaRepositorio.findById(noticiaId)
                .orElseThrow(() -> new RuntimeException("Noticia no encontrada"));
        UsuarioEntidad usuario = usuarioRepositorio.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        ComentarioEntidad comentario = new ComentarioEntidad();
        comentario.setNoticia(noticia);
        comentario.setAutor(usuario);
        comentario.setContenido(contenido);
        comentario.setFecha(LocalDateTime.now());

        return comentarioRepositorio.save(comentario);
    }

    public void eliminarComentario(Integer id, Integer usuarioSolicitanteId, boolean esAdmin) {
        ComentarioEntidad comentario = comentarioRepositorio.findById(id)
                .orElseThrow(() -> new RuntimeException("Comentario no encontrado"));

        if (!esAdmin && !comentario.getAutor().getId().equals(usuarioSolicitanteId)) {
            throw new RuntimeException("No tienes permiso para eliminar este comentario");
        }
        comentarioRepositorio.delete(comentario);
    }

    public List<ComentarioEntidad> listarPorNoticia(Integer noticiaId) {
        return comentarioRepositorio.findByNoticiaIdOrderByFechaDesc(noticiaId);
    }

    public long contarComentarios(Integer noticiaId) {
        return comentarioRepositorio.countByNoticiaId(noticiaId);
    }
}
