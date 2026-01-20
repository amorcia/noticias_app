package com.noticias.api.servicios;

import com.noticias.api.dtos.NoticiaDTO;
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
    private final com.noticias.api.repositorios.VotoRepositorio votoRepositorio;
    private final com.noticias.api.repositorios.UsuarioRepositorio usuarioRepositorio;

    public NoticiaServicio(NoticiaRepositorio noticiaRepositorio,
            NoticiaEliminadaRepositorio noticiaEliminadaRepositorio,
            com.noticias.api.repositorios.VotoRepositorio votoRepositorio,
            com.noticias.api.repositorios.UsuarioRepositorio usuarioRepositorio) {
        this.noticiaRepositorio = noticiaRepositorio;
        this.noticiaEliminadaRepositorio = noticiaEliminadaRepositorio;
        this.votoRepositorio = votoRepositorio;
        this.usuarioRepositorio = usuarioRepositorio;
    }

    public List<NoticiaDTO> listarTodas() {
        return noticiaRepositorio.findAll().stream().map(this::convertirADTO).toList();
    }

    public List<NoticiaDTO> listarDestacadas() {
        return noticiaRepositorio.findByDestacadaTrue().stream().map(this::convertirADTO).toList();
    }

    public List<NoticiaDTO> listarPopulares() {
        return noticiaRepositorio.findTopPopular(org.springframework.data.domain.PageRequest.of(0, 5)).stream()
                .map(this::convertirADTO).toList();
    }

    public List<NoticiaDTO> listarPorCategoria(Integer categoriaId) {
        return noticiaRepositorio.findByCategoriaId(categoriaId).stream().map(this::convertirADTO).toList();
    }

    public List<NoticiaDTO> listarPorCategoriaNombre(String nombre) {
        return noticiaRepositorio.findByCategoriaNombre(nombre).stream().map(this::convertirADTO).toList();
    }

    public boolean existePorTitulo(String titulo) {
        return noticiaRepositorio.existsByTitulo(titulo);
    }

    public List<NoticiaDTO> listarPorCategoriaFiltrado(Integer catId, String filtro, Integer mes, Integer anio) {
        List<NoticiaEntidad> noticias;
        if ("recientes".equals(filtro)) {
            noticias = noticiaRepositorio.findByCategoriaIdOrderByFechaPublicacionDesc(catId);
        } else if ("mes".equals(filtro) && mes != null && anio != null) {
            noticias = noticiaRepositorio.findByCategoriaIdAndMesAndAnio(catId, mes, anio);
        } else if ("mejores".equals(filtro)) {
            noticias = noticiaRepositorio.findByCategoriaIdOrdenPorValoracionDesc(catId);
        } else if ("peores".equals(filtro)) {
            noticias = noticiaRepositorio.findByCategoriaIdOrdenPorValoracionAsc(catId);
        } else {
            noticias = noticiaRepositorio.findByCategoriaId(catId); // Default
        }
        return noticias.stream().map(this::convertirADTO).toList();
    }

    public List<NoticiaDTO> listarPorCategoriaNombreYTipo(String nombre, Boolean esAportacion) {
        return noticiaRepositorio.findByCategoriaNombreAndEsAportacionUsuario(nombre, esAportacion).stream()
                .map(this::convertirADTO).toList();
    }

    public List<NoticiaDTO> listarPorAutor(Integer autorId) {
        return noticiaRepositorio.findByAutorId(autorId).stream().map(this::convertirADTO).toList();
    }

    public Optional<NoticiaDTO> buscarPorId(Integer id) {
        if (id == null)
            return Optional.empty();
        return noticiaRepositorio.findById(id).map(this::convertirADTO);
    }

    public Optional<NoticiaDTO> buscarPorTitulo(String titulo) {
        if (titulo == null || titulo.isBlank())
            return Optional.empty();
        return noticiaRepositorio.findByTitulo(titulo).map(this::convertirADTO);
    }

    @Transactional
    public NoticiaDTO crearNoticia(NoticiaEntidad noticia) {
        if (noticia == null)
            throw new IllegalArgumentException("Noticia cannot be null");
        if (noticiaRepositorio.existsByTitulo(noticia.getTitulo())) {
            throw new IllegalArgumentException("Ya existe una noticia con este título");
        }
        if (noticia.getFechaPublicacion() == null) {
            noticia.setFechaPublicacion(LocalDateTime.now());
        }
        if (noticia.getVisitas() == null) {
            noticia.setVisitas(0);
        }
        return convertirADTO(noticiaRepositorio.save(noticia));
    }

    @Transactional
    public NoticiaDTO actualizarNoticia(Integer id, NoticiaEntidad noticiaActualizada) {
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
            return convertirADTO(noticiaRepositorio.save(noticia));
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

    @Transactional(readOnly = true)
    public String obtenerTipoVoto(Integer noticiaId, Integer usuarioId) {
        if (noticiaId == null || usuarioId == null)
            return "NONE";
        return votoRepositorio.findByUsuarioAndNoticia(
                usuarioRepositorio.getReferenceById(usuarioId),
                noticiaRepositorio.getReferenceById(noticiaId))
                .map(com.noticias.api.entidades.VotoEntidad::getTipo)
                .orElse("NONE");
    }

    @Transactional
    public boolean votar(Integer id, boolean isLike, Integer usuarioId) {
        if (id == null || usuarioId == null)
            return false;

        NoticiaEntidad noticia = noticiaRepositorio.findById(id).orElse(null);
        com.noticias.api.entidades.UsuarioEntidad usuario = usuarioRepositorio.findById(usuarioId).orElse(null);

        if (noticia == null || usuario == null)
            return false;

        String tipoNuevo = isLike ? "LIKE" : "DISLIKE";
        Optional<com.noticias.api.entidades.VotoEntidad> votoExistente = votoRepositorio.findByUsuarioAndNoticia(
                usuario,
                noticia);

        if (votoExistente.isPresent()) {
            com.noticias.api.entidades.VotoEntidad voto = votoExistente.get();
            if (voto.getTipo().equals(tipoNuevo)) {
                // Si es el mismo tipo, quitamos el voto
                votoRepositorio.delete(voto);
                if (isLike)
                    noticia.setLikes(noticia.getLikes() - 1);
                else
                    noticia.setDislikes(noticia.getDislikes() - 1);
            } else {
                // Si es tipo distinto, cambiamos el voto
                voto.setTipo(tipoNuevo);
                voto.setFecha(LocalDateTime.now());
                votoRepositorio.save(voto);
                if (isLike) {
                    noticia.setLikes(noticia.getLikes() + 1);
                    noticia.setDislikes(noticia.getDislikes() - 1);
                } else {
                    noticia.setLikes(noticia.getLikes() - 1);
                    noticia.setDislikes(noticia.getDislikes() + 1);
                }
            }
        } else {
            // No hay voto previo, creamos uno nuevo
            com.noticias.api.entidades.VotoEntidad nuevoVoto = new com.noticias.api.entidades.VotoEntidad();
            nuevoVoto.setNoticia(noticia);
            nuevoVoto.setUsuario(usuario);
            nuevoVoto.setTipo(tipoNuevo);
            nuevoVoto.setFecha(LocalDateTime.now());
            votoRepositorio.save(nuevoVoto);

            if (isLike)
                noticia.setLikes(noticia.getLikes() + 1);
            else
                noticia.setDislikes(noticia.getDislikes() + 1);
        }

        noticiaRepositorio.save(noticia);
        return true;
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

    @Transactional
    public boolean eliminarNoticiaPorTitulo(String titulo, String motivo,
            com.noticias.api.entidades.UsuarioEntidad eliminador) {
        if (titulo == null || titulo.isBlank())
            return false;
        Optional<NoticiaEntidad> noticiaOpt = noticiaRepositorio.findByTitulo(titulo);
        if (noticiaOpt.isPresent()) {
            return eliminarNoticia(noticiaOpt.get().getId(), motivo, eliminador);
        }
        return false;
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

    private NoticiaDTO convertirADTO(NoticiaEntidad entidad) {
        if (entidad == null)
            return null;
        NoticiaDTO dto = new NoticiaDTO();
        dto.setId(entidad.getId());
        dto.setTitulo(entidad.getTitulo());
        dto.setSubtitulo(entidad.getSubtitulo());
        dto.setContenido(entidad.getContenido());
        dto.setImagenUrl(entidad.getImagenUrl()); // Can be null
        dto.setFechaPublicacion(entidad.getFechaPublicacion());
        dto.setVisitas(entidad.getVisitas());
        dto.setDestacada(entidad.getDestacada());
        dto.setEsAportacionUsuario(entidad.getEsAportacionUsuario());
        dto.setLikes(entidad.getLikes());
        dto.setDislikes(entidad.getDislikes());
        dto.setComentariosCount(entidad.getComentariosCount());

        if (entidad.getAutor() != null) {
            dto.setAutorId(entidad.getAutor().getId());
            dto.setAutorNombre(entidad.getAutor().getNombreCompleto());
            dto.setAutorImagenUrl(entidad.getAutor().getImagenUrl());
        }
        if (entidad.getCategoria() != null) {
            dto.setCategoriaId(entidad.getCategoria().getId());
            dto.setCategoriaNombre(entidad.getCategoria().getNombre());
            dto.setCategoriaColor(entidad.getCategoria().getColor());
        }
        return dto;
    }
}
