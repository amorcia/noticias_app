package com.noticias.api.servicios;

import com.noticias.api.entidades.DenunciaEntidad;
import com.noticias.api.entidades.NoticiaEntidad;
import com.noticias.api.entidades.UsuarioEntidad;
import com.noticias.api.entidades.ComentarioEntidad;
import com.noticias.api.repositorios.DenunciaRepositorio;
import com.noticias.api.repositorios.NoticiaRepositorio;
import com.noticias.api.repositorios.UsuarioRepositorio;
import com.noticias.api.repositorios.ComentarioRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class DenunciaServicio {

        @Autowired
        private DenunciaRepositorio denunciaRepositorio;
        @Autowired
        private NoticiaRepositorio noticiaRepositorio;
        @Autowired
        private UsuarioRepositorio usuarioRepositorio;
        @Autowired
        private ComentarioRepositorio comentarioRepositorio;

        public DenunciaEntidad crearDenunciaNoticia(Integer noticiaId, Integer usuarioId, String motivo,
                        String descripcion) {
                if (noticiaId == null || usuarioId == null) {
                        throw new RuntimeException("ID de noticia o usuario nulo");
                }
                NoticiaEntidad noticia = noticiaRepositorio.findById(noticiaId)
                                .orElseThrow(() -> new RuntimeException("Noticia no encontrada"));
                UsuarioEntidad denunciante = usuarioRepositorio.findById(usuarioId)
                                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

                DenunciaEntidad denuncia = new DenunciaEntidad();
                denuncia.setNoticia(noticia);
                denuncia.setDenunciante(denunciante);
                denuncia.setMotivo(motivo);
                denuncia.setDescripcion(descripcion);
                denuncia.setFecha(LocalDateTime.now());

                return denunciaRepositorio.save(denuncia);
        }

        public DenunciaEntidad crearDenunciaComentario(Integer comentarioId, Integer usuarioId, String motivo,
                        String descripcion) {
                if (comentarioId == null || usuarioId == null) {
                        throw new RuntimeException("ID de comentario o usuario nulo");
                }
                ComentarioEntidad comentario = comentarioRepositorio.findById(comentarioId)
                                .orElseThrow(() -> new RuntimeException("Comentario no encontrado"));
                UsuarioEntidad denunciante = usuarioRepositorio.findById(usuarioId)
                                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

                DenunciaEntidad denuncia = new DenunciaEntidad();
                denuncia.setComentario(comentario);
                denuncia.setDenunciante(denunciante);
                denuncia.setMotivo(motivo);
                denuncia.setDescripcion(descripcion);
                denuncia.setFecha(LocalDateTime.now());

                return denunciaRepositorio.save(denuncia);
        }

        public List<DenunciaEntidad> listarTodas() {
                return denunciaRepositorio.findAllByOrderByFechaDesc();
        }
}
