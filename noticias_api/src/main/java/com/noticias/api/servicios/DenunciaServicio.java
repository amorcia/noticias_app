package com.noticias.api.servicios;

import com.noticias.api.entidades.DenunciaEntidad;
import com.noticias.api.entidades.NoticiaEntidad;
import com.noticias.api.entidades.UsuarioEntidad;
import com.noticias.api.repositorios.DenunciaRepositorio;
import com.noticias.api.repositorios.NoticiaRepositorio;
import com.noticias.api.repositorios.UsuarioRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class DenunciaServicio {

        @Autowired
        private DenunciaRepositorio denunciaRepositorio;
        @Autowired
        private NoticiaRepositorio noticiaRepositorio;
        @Autowired
        private UsuarioRepositorio usuarioRepositorio;

        public DenunciaEntidad crearDenunciaNoticia(Integer noticiaId, Integer usuarioId, String motivo,
                        String descripcion) {
                if (noticiaId == null)
                        throw new IllegalArgumentException("El ID de la noticia no puede ser nulo");
                if (usuarioId == null)
                        throw new IllegalArgumentException("El ID del usuario no puede ser nulo");

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

        public java.util.List<DenunciaEntidad> listarTodas() {
                return denunciaRepositorio.findAllByOrderByFechaDesc();
        }
}
