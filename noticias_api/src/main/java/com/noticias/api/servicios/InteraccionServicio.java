package com.noticias.api.servicios;

import com.noticias.api.entidades.NoticiaEntidad;

import com.noticias.api.repositorios.VotoRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InteraccionServicio {

    @Autowired
    private VotoRepositorio votoRepositorio;

    public long contarDislikes(NoticiaEntidad noticia) {
        return votoRepositorio.countByNoticiaAndTipo(noticia, "DISLIKE");
    }
}
