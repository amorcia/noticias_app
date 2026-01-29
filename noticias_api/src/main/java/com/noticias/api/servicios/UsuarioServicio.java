package com.noticias.api.servicios;

import com.noticias.api.entidades.UsuarioEntidad;
import com.noticias.api.repositorios.UsuarioRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Servicio para gestión de usuarios.
 * NOTA: Este servicio NO maneja seguridad ni encriptación.
 * Toda la seguridad se gestiona en el backend web (noticias_web).
 * Las contraseñas llegan ya encriptadas desde el backend web.
 */
@Service
@SuppressWarnings("null")
public class UsuarioServicio {

    private final UsuarioRepositorio usuarioRepositorio;

    public UsuarioServicio(UsuarioRepositorio usuarioRepositorio) {
        this.usuarioRepositorio = usuarioRepositorio;
    }

    public List<UsuarioEntidad> listarTodos() {
        return usuarioRepositorio.findAll();
    }

    public Optional<UsuarioEntidad> buscarPorId(Integer id) {
        if (id == null)
            return Optional.empty();
        return usuarioRepositorio.findById(id);
    }

    public Optional<UsuarioEntidad> buscarPorEmail(String email) {
        return usuarioRepositorio.findByEmail(email);
    }

    @Transactional
    public UsuarioEntidad crearUsuario(UsuarioEntidad usuario) {
        // Logica simplificada: solo guardar.
        // La asignación de roles especiales o encriptación debe venir resuelta o
        // manejada por quien llama (Web o Initializer).
        return usuarioRepositorio.save(usuario);
    }

    @Transactional
    public UsuarioEntidad actualizarUsuario(Integer id, UsuarioEntidad usuarioActualizado) {
        if (id == null)
            return null;
        return usuarioRepositorio.findById(id).map(usuario -> {
            boolean changed = false;
            if (usuarioActualizado.getNombreCompleto() != null) {
                usuario.setNombreCompleto(usuarioActualizado.getNombreCompleto());
                changed = true;
            }
            if (usuarioActualizado.getEmail() != null) {
                usuario.setEmail(usuarioActualizado.getEmail());
                changed = true;
            }
            if (usuarioActualizado.getMovil() != null) {
                usuario.setMovil(usuarioActualizado.getMovil());
                changed = true;
            }
            if (usuarioActualizado.getPassword() != null) {
                usuario.setPassword(usuarioActualizado.getPassword());
                changed = true;
            }
            if (usuarioActualizado.getRol() != null) {
                usuario.setRol(usuarioActualizado.getRol());
                changed = true;
            }
            if (usuarioActualizado.getActivo() != null) {
                usuario.setActivo(usuarioActualizado.getActivo());
                changed = true;
            }
            if (usuarioActualizado.getCodigoVerificacion() != null) {
                usuario.setCodigoVerificacion(usuarioActualizado.getCodigoVerificacion());
                changed = true;
            }
            if (usuarioActualizado.getTokenSession() != null) {
                usuario.setTokenSession(usuarioActualizado.getTokenSession());
                changed = true;
            }
            if (usuarioActualizado.getVetado() != null) {
                usuario.setVetado(usuarioActualizado.getVetado());
                changed = true;
            }
            if (usuarioActualizado.getMotivoVeto() != null) {
                usuario.setMotivoVeto(usuarioActualizado.getMotivoVeto());
                changed = true;
            }
            if (usuarioActualizado.getFechaVeto() != null) {
                usuario.setFechaVeto(usuarioActualizado.getFechaVeto());
                changed = true;
            }
            if (usuarioActualizado.getSecretKey2FA() != null) {
                usuario.setSecretKey2FA(usuarioActualizado.getSecretKey2FA());
                changed = true;
            }
            if (usuarioActualizado.getImagenUrl() != null) {
                usuario.setImagenUrl(usuarioActualizado.getImagenUrl());
                changed = true;
            }
            if (usuarioActualizado.getEmailPendiente() != null) {
                usuario.setEmailPendiente(usuarioActualizado.getEmailPendiente());
                changed = true;
            }
            if (changed) {
                return usuarioRepositorio.save(usuario);
            }
            return usuario;
        }).orElse(null);
    }

    @Transactional
    public boolean confirmarEmail(String token) {
        // Buscar por codigo y activar. Esto puede considerarse lógica de negocio
        // pero es una operación atómica de DB: "Activate user by token".
        // Lo mantengo por simplicidad de la API CRUD extendida.
        Optional<UsuarioEntidad> usuarioOpt = usuarioRepositorio.findByCodigoVerificacion(token);
        if (usuarioOpt.isPresent()) {
            UsuarioEntidad usuario = usuarioOpt.get();
            usuario.setActivo(true);
            usuario.setCodigoVerificacion(null);

            // Si hay email pendiente, aplicarlo ahora
            if (usuario.getEmailPendiente() != null) {
                usuario.setEmail(usuario.getEmailPendiente());
                usuario.setEmailPendiente(null);
            }

            usuarioRepositorio.save(usuario);
            return true;
        }
        return false;
    }

    @Transactional
    public String generarTokenRecuperacion(String email) {
        // Genera y guarda token. Operación de persistencia de token.
        Optional<UsuarioEntidad> usuarioOpt = usuarioRepositorio.findByEmail(email);
        if (usuarioOpt.isPresent()) {
            UsuarioEntidad usuario = usuarioOpt.get();
            String token = String.valueOf((int) ((Math.random() * 900000) + 100000));
            usuario.setCodigoVerificacion(token);
            usuarioRepositorio.save(usuario);
            return token;
        }
        return null;
    }

    @Transactional
    public boolean restablecerPassword(String token, String nuevaPasswordEncriptada) {
        // Actualiza password por token.
        Optional<UsuarioEntidad> usuarioOpt = usuarioRepositorio.findByCodigoVerificacion(token);
        if (usuarioOpt.isPresent()) {
            UsuarioEntidad usuario = usuarioOpt.get();
            usuario.setPassword(nuevaPasswordEncriptada);
            usuario.setCodigoVerificacion(null);
            usuarioRepositorio.save(usuario);
            return true;
        }
        return false;
    }

    @Transactional
    public boolean vetarUsuario(Integer id, String motivo, String duracion) {
        if (id == null)
            return false;
        return usuarioRepositorio.findById(id).map(usuario -> {
            usuario.setVetado(true);
            usuario.setMotivoVeto(motivo);
            usuario.setFechaVeto(LocalDateTime.now());
            usuario.setActivo(false);

            // Calcular fecha fin veto
            LocalDateTime fin = null;
            if (duracion != null && !duracion.equals("PERMANENTE")) {
                LocalDateTime now = LocalDateTime.now();
                switch (duracion) {
                    case "24H":
                        fin = now.plusDays(1);
                        break;
                    case "3D":
                        fin = now.plusDays(3);
                        break;
                    case "1W":
                        fin = now.plusWeeks(1);
                        break;
                    case "1M":
                        fin = now.plusMonths(1);
                        break;
                    default:
                        fin = null; // Permanente if invalid or unspecified
                }
            } else {
                fin = null; // Permanente means null date (or we could use max date)
            }
            usuario.setVetadoHasta(fin);

            usuarioRepositorio.save(usuario);
            return true;
        }).orElse(false);
    }

    @Transactional
    public boolean desvetarUsuario(Integer id) {
        if (id == null)
            return false;
        return usuarioRepositorio.findById(id).map(usuario -> {
            usuario.setVetado(false);
            usuario.setMotivoVeto(null);
            usuario.setFechaVeto(null);
            usuario.setActivo(true);
            usuarioRepositorio.save(usuario);
            return true;
        }).orElse(false);
    }

    @Transactional
    public boolean eliminarUsuario(Integer id) {
        if (id != null && usuarioRepositorio.existsById(id)) {
            // Eliminamos la lógica de "Protección Owner" aquí si queremos ser FULL DUMB,
            // pero es una restricción de integridad importante.
            // El usuario dijo "API solo Queries y DB".
            // Voy a eliminar la logica de Negocio Explicita de "antoniowebserver"
            // y dejar que la DB o el Web controlen permisos.
            // Si el Web tiene proteccion, no llegará aquí.
            usuarioRepositorio.deleteById(id);
            return true;
        }
        return false;
    }

    @Transactional
    public boolean cerrarSesion(Integer id) {
        if (id == null)
            return false;
        return usuarioRepositorio.findById(id).map(usuario -> {
            usuario.setTokenSession(null);
            usuarioRepositorio.save(usuario);
            return true;
        }).orElse(false);
    }

    @Transactional
    public boolean desactivar2FA(Integer id) {
        if (id == null)
            return false;
        return usuarioRepositorio.findById(id).map(usuario -> {
            usuario.setSecretKey2FA(null);
            usuarioRepositorio.save(usuario);
            return true;
        }).orElse(false);
    }

    @Transactional
    public boolean eliminarImagen(Integer id) {
        if (id == null)
            return false;
        return usuarioRepositorio.findById(id).map(usuario -> {
            usuario.setImagenUrl(null);
            usuarioRepositorio.save(usuario);
            return true;
        }).orElse(false);
    }
}
