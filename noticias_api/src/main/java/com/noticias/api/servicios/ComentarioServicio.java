package com.noticias.api.servicios;

import com.noticias.api.entidades.ComentarioEntidad;
import com.noticias.api.entidades.NoticiaEntidad;
import com.noticias.api.entidades.UsuarioEntidad;
import com.noticias.api.repositorios.ComentarioRepositorio;
import com.noticias.api.repositorios.NoticiaRepositorio;
import com.noticias.api.repositorios.UsuarioRepositorio;
import com.noticias.api.repositorios.VotoComentarioRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ComentarioServicio {

    @Autowired
    private ComentarioRepositorio comentarioRepositorio;
    @Autowired
    private NoticiaRepositorio noticiaRepositorio;
    @Autowired
    private UsuarioRepositorio usuarioRepositorio;
    @Autowired
    private VotoComentarioRepositorio votoComentarioRepositorio;

    public ComentarioEntidad crearComentario(Integer noticiaId, Integer usuarioId, String contenido) {
        return crearComentario(noticiaId, usuarioId, contenido, null);
    }

    public ComentarioEntidad crearComentario(Integer noticiaId, Integer usuarioId, String contenido, Integer padreId) {
        if (noticiaId == null || usuarioId == null) {
            throw new RuntimeException("ID de noticia o usuario nulo");
        }
        NoticiaEntidad noticia = noticiaRepositorio.findById(noticiaId)
                .orElseThrow(() -> new RuntimeException("Noticia no encontrada"));
        UsuarioEntidad usuario = usuarioRepositorio.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        ComentarioEntidad comentario = new ComentarioEntidad();
        comentario.setNoticia(noticia);
        comentario.setAutor(usuario);
        comentario.setContenido(contenido);
        comentario.setFecha(LocalDateTime.now());

        if (padreId != null) {
            ComentarioEntidad padre = comentarioRepositorio.findById(padreId)
                    .orElseThrow(() -> new RuntimeException("Comentario padre no encontrado"));
            comentario.setPadre(padre);
        }

        ComentarioEntidad guardado = comentarioRepositorio.save(comentario);

        // Incrementar contador en noticia
        noticia.setComentariosCount(noticia.getComentariosCount() + 1);
        noticiaRepositorio.save(noticia);

        return guardado;
    }

    public void eliminarComentario(Integer id, Integer usuarioSolicitanteId, boolean esAdmin) {
        if (id == null) {
            throw new RuntimeException("ID de comentario nulo");
        }
        ComentarioEntidad comentario = comentarioRepositorio.findById(id)
                .orElseThrow(() -> new RuntimeException("Comentario no encontrado"));

        if (!esAdmin && !comentario.getAutor().getId().equals(usuarioSolicitanteId)) {
            throw new RuntimeException("No tienes permiso para eliminar este comentario");
        }

        NoticiaEntidad noticia = comentario.getNoticia();
        comentarioRepositorio.delete(comentario);

        // Decrementar contador
        if (noticia != null) {
            noticia.setComentariosCount(Math.max(0, noticia.getComentariosCount() - 1));
            noticiaRepositorio.save(noticia);
        }
    }

    @Transactional
    public boolean votar(Integer id, boolean isLike, Integer usuarioId) {
        if (id == null || usuarioId == null) {
            return false;
        }
        ComentarioEntidad comentario = comentarioRepositorio.findById(id).orElse(null);
        UsuarioEntidad usuario = usuarioRepositorio.findById(usuarioId).orElse(null);
        if (comentario == null || usuario == null) {
            return false;
        }

        String tipoNuevo = isLike ? "LIKE" : "DISLIKE";
        Optional<com.noticias.api.entidades.VotoComentarioEntidad> votoExistente = votoComentarioRepositorio
                .findByUsuarioAndComentario(usuario, comentario);

        if (votoExistente.isPresent()) {
            com.noticias.api.entidades.VotoComentarioEntidad voto = votoExistente.get();
            if (voto.getTipo().equals(tipoNuevo)) {
                votoComentarioRepositorio.delete(voto);
                if (isLike)
                    comentario.setLikes(comentario.getLikes() - 1);
                else
                    comentario.setDislikes(comentario.getDislikes() - 1);
            } else {
                voto.setTipo(tipoNuevo);
                voto.setFecha(LocalDateTime.now());
                votoComentarioRepositorio.save(voto);
                if (isLike) {
                    comentario.setLikes(comentario.getLikes() + 1);
                    comentario.setDislikes(comentario.getDislikes() - 1);
                } else {
                    comentario.setLikes(comentario.getLikes() - 1);
                    comentario.setDislikes(comentario.getDislikes() + 1);
                }
            }
        } else {
            com.noticias.api.entidades.VotoComentarioEntidad nuevoVoto = new com.noticias.api.entidades.VotoComentarioEntidad();
            nuevoVoto.setComentario(comentario);
            nuevoVoto.setUsuario(usuario);
            nuevoVoto.setTipo(tipoNuevo);
            votoComentarioRepositorio.save(nuevoVoto);
            if (isLike)
                comentario.setLikes(comentario.getLikes() + 1);
            else
                comentario.setDislikes(comentario.getDislikes() + 1);
        }
        comentarioRepositorio.save(comentario);
        return true;
    }

    public List<ComentarioEntidad> listarPorNoticia(Integer noticiaId, Integer usuarioId) {
        // Obtenemos solo los comentarios de nivel superior (sin padre)
        List<ComentarioEntidad> raices = comentarioRepositorio.findByNoticiaIdAndPadreIsNullOrderByFechaDesc(noticiaId);

        if (usuarioId != null) {
            UsuarioEntidad usuario = usuarioRepositorio.findById(usuarioId).orElse(null);
            if (usuario != null) {
                raices.forEach(c -> populateVotos(c, usuario));
            }
        }

        return raices;
    }

    private void populateVotos(ComentarioEntidad c, UsuarioEntidad usuario) {
        votoComentarioRepositorio.findByUsuarioAndComentario(usuario, c)
                .ifPresent(v -> c.setVotoUsuario(v.getTipo()));

        if (c.getRespuestas() != null) {
            c.getRespuestas().forEach(r -> populateVotos(r, usuario));
        }
    }

    public long contarComentarios(Integer noticiaId) {
        return comentarioRepositorio.countByNoticiaId(noticiaId);
    }
}
