package com.noticias.api.servicios;

import com.noticias.api.entidades.NoticiaEntidad;
import com.noticias.api.repositorios.NoticiaRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Servicio para gestión de noticias.
 */
@Service
@SuppressWarnings("null")
public class NoticiaServicio {

    private final NoticiaRepositorio noticiaRepositorio;

    public NoticiaServicio(NoticiaRepositorio noticiaRepositorio) {
        this.noticiaRepositorio = noticiaRepositorio;
    }

    public List<NoticiaEntidad> listarTodas() {
        return noticiaRepositorio.findAll();
    }

    public List<NoticiaEntidad> listarDestacadas() {
        return noticiaRepositorio.findByDestacadaTrue();
    }

    public List<NoticiaEntidad> listarPopulares() {
        return noticiaRepositorio.findTopPopular(org.springframework.data.domain.PageRequest.of(0, 5));
    }

    public List<NoticiaEntidad> listarPorCategoria(Integer categoriaId) {
        return noticiaRepositorio.findByCategoriaId(categoriaId);
    }

    public List<NoticiaEntidad> listarPorCategoriaNombre(String nombre) {
        return noticiaRepositorio.findByCategoriaNombre(nombre);
    }

    public List<NoticiaEntidad> listarPorCategoriaNombreYTipo(String nombre, Boolean esAportacion) {
        return noticiaRepositorio.findByCategoriaNombreAndEsAportacionUsuario(nombre, esAportacion);
    }

    public List<NoticiaEntidad> listarPorAutor(Integer autorId) {
        return noticiaRepositorio.findByAutorId(autorId);
    }

    public Optional<NoticiaEntidad> buscarPorId(Integer id) {
        if (id == null)
            return Optional.empty();
        return noticiaRepositorio.findById(id);
    }

    @Transactional
    public NoticiaEntidad crearNoticia(NoticiaEntidad noticia) {
        if (noticia == null)
            throw new IllegalArgumentException("Noticia cannot be null");
        if (noticia.getFechaPublicacion() == null) {
            noticia.setFechaPublicacion(LocalDateTime.now());
        }
        if (noticia.getVisitas() == null) {
            noticia.setVisitas(0);
        }
        return noticiaRepositorio.save(noticia);
    }

    @Transactional
    public NoticiaEntidad actualizarNoticia(Integer id, NoticiaEntidad noticiaActualizada) {
        if (id == null)
            return null;
        return noticiaRepositorio.findById(id).map(noticia -> {
            if (noticiaActualizada.getTitulo() != null) {
                noticia.setTitulo(noticiaActualizada.getTitulo());
            }
            if (noticiaActualizada.getSubtitulo() != null) {
                noticia.setSubtitulo(noticiaActualizada.getSubtitulo());
            }
            if (noticiaActualizada.getContenido() != null) {
                noticia.setContenido(noticiaActualizada.getContenido());
            }
            if (noticiaActualizada.getImagenUrl() != null) {
                noticia.setImagenUrl(noticiaActualizada.getImagenUrl());
            }
            if (noticiaActualizada.getCategoria() != null) {
                noticia.setCategoria(noticiaActualizada.getCategoria());
            }
            if (noticiaActualizada.getDestacada() != null) {
                noticia.setDestacada(noticiaActualizada.getDestacada());
            }
            return noticiaRepositorio.save(noticia);
        }).orElse(null);
    }

    @Transactional
    public boolean incrementarVisitas(Integer id) {
        if (id == null)
            return false;
        return noticiaRepositorio.findById(id).map(noticia -> {
            noticia.setVisitas(noticia.getVisitas() + 1);
            noticiaRepositorio.save(noticia);
            return true;
        }).orElse(false);
    }

    @Transactional
    public boolean eliminarNoticia(Integer id) {
        if (id != null && noticiaRepositorio.existsById(id)) {
            noticiaRepositorio.deleteById(id);
            return true;
        }
        return false;
    }
}
