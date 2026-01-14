package com.noticias.api.servicios;

import com.noticias.api.entidades.NoticiaEntidad;
import com.noticias.api.entidades.NoticiaEliminadaEntidad;
import com.noticias.api.repositorios.NoticiaRepositorio;
import com.noticias.api.repositorios.NoticiaEliminadaRepositorio;
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
    private final NoticiaEliminadaRepositorio noticiaEliminadaRepositorio;

    public NoticiaServicio(NoticiaRepositorio noticiaRepositorio,
            NoticiaEliminadaRepositorio noticiaEliminadaRepositorio) {
        this.noticiaRepositorio = noticiaRepositorio;
        this.noticiaEliminadaRepositorio = noticiaEliminadaRepositorio;
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

    public boolean existePorTitulo(String titulo) {
        return noticiaRepositorio.existsByTitulo(titulo);
    }

    public List<NoticiaEntidad> listarPorCategoriaFiltrado(Integer catId, String filtro, Integer mes, Integer anio) {
        if ("recientes".equals(filtro)) {
            return noticiaRepositorio.findByCategoriaIdOrderByFechaPublicacionDesc(catId);
        } else if ("mes".equals(filtro) && mes != null && anio != null) {
            return noticiaRepositorio.findByCategoriaIdAndMesAndAnio(catId, mes, anio);
        } else if ("mejores".equals(filtro)) {
            return noticiaRepositorio.findByCategoriaIdOrdenPorValoracionDesc(catId);
        } else if ("peores".equals(filtro)) {
            return noticiaRepositorio.findByCategoriaIdOrdenPorValoracionAsc(catId);
        }
        return noticiaRepositorio.findByCategoriaId(catId); // Default
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
    public boolean votar(Integer id, boolean isLike) {
        if (id == null)
            return false;
        return noticiaRepositorio.findById(id).map(noticia -> {
            if (isLike) {
                noticia.setLikes(noticia.getLikes() + 1);
            } else {
                noticia.setDislikes(noticia.getDislikes() + 1);
            }
            noticiaRepositorio.save(noticia);
            return true;
        }).orElse(false);
    }

    @Transactional
    public boolean eliminarNoticia(Integer id, String motivo, com.noticias.api.entidades.UsuarioEntidad eliminador) {
        if (id != null && noticiaRepositorio.existsById(id)) {
            NoticiaEntidad noticia = noticiaRepositorio.findById(id).orElse(null);
            if (noticia != null) {
                // Archivar
                String rol = eliminador != null && eliminador.getRol() != null ? eliminador.getRol().getNombre()
                        : "DESCONOCIDO";
                String eliminadoPor = eliminador != null ? eliminador.getEmail() : "Sistema";
                String motivoFinal = motivo != null && !motivo.isBlank() ? motivo : "Sin motivo especificado";

                com.noticias.api.entidades.NoticiaEliminadaEntidad eliminada = new com.noticias.api.entidades.NoticiaEliminadaEntidad(
                        noticia, motivoFinal, eliminadoPor, rol);
                noticiaEliminadaRepositorio.save(eliminada);

                // Eliminar
                noticiaRepositorio.deleteById(id);
                return true;
            }
        }
        return false;
    }

    @Transactional
    public boolean eliminarNoticia(Integer id) {
        return eliminarNoticia(id, "Eliminación directa", null);
    }

    @Transactional(readOnly = true)
    public List<NoticiaEliminadaEntidad> listarNoticiasEliminadasPorAdmin() {
        // Asumiendo que ADMIN y TRABAJADOR son roles de administración
        return noticiaEliminadaRepositorio.findByRolEliminadorIn(List.of("ADMIN", "TRABAJADOR", "OWNER"));
        // Or if user specifically wants NON-OWNER (since owner deletions are boring):
        // "solo si la ha borrado alguien del equipo de administracion y no el
        // propietario de la noticia"
        // Need to check if logic should exclude Self-Deletions?
        // User says: "no el propietario de la noticia".
        // My archiving logic saves 'rolEliminador'. If owner deletes his own news, role
        // might be OWNER but relation is Own.
        // But here we filter by WHO deleted it. If Admin deletes it -> Show. if
        // User(Owner) deletes it -> Hide.
        // We will filter by Roles that represent Administration acting on others.
    }
}
