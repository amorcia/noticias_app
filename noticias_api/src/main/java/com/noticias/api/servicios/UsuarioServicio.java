package com.noticias.api.servicios;

import com.noticias.api.entidades.UsuarioEntidad;
import com.noticias.api.repositorios.UsuarioRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
// UUID removed

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
        // No se encripta aquí - la contraseña ya viene encriptada del backend web

        // CHECK OWNER EMAIL
        if (usuario.getEmail().equalsIgnoreCase("antoniowebserver@gmail.com")) {
            com.noticias.api.entidades.RolEntidad rolOwner = rolRepositorio.findByNombre("Owner")
                    .orElse(null); // Should exist due to PostConstruct
            if (rolOwner != null) {
                usuario.setRol(rolOwner);
                usuario.setEsSuperAdmin(true);
            }
        }

        return usuarioRepositorio.save(usuario);
    }

    @Transactional
    public UsuarioEntidad actualizarUsuario(Integer id, UsuarioEntidad usuarioActualizado) {
        if (id == null)
            return null;
        return usuarioRepositorio.findById(id).map(usuario -> {
            if (usuarioActualizado.getNombreCompleto() != null) {
                usuario.setNombreCompleto(usuarioActualizado.getNombreCompleto());
            }
            if (usuarioActualizado.getEmail() != null) {
                usuario.setEmail(usuarioActualizado.getEmail());
            }
            if (usuarioActualizado.getMovil() != null) {
                usuario.setMovil(usuarioActualizado.getMovil());
            }
            if (usuarioActualizado.getPassword() != null) {
                // La contraseña ya viene encriptada
                usuario.setPassword(usuarioActualizado.getPassword());
            }
            if (usuarioActualizado.getRol() != null) {
                usuario.setRol(usuarioActualizado.getRol());
            }
            if (usuarioActualizado.getActivo() != null) {
                usuario.setActivo(usuarioActualizado.getActivo());
            }
            if (usuarioActualizado.getCodigoVerificacion() != null) {
                usuario.setCodigoVerificacion(usuarioActualizado.getCodigoVerificacion());
            }
            if (usuarioActualizado.getTokenSession() != null) {
                usuario.setTokenSession(usuarioActualizado.getTokenSession());
            }
            if (usuarioActualizado.getVetado() != null) {
                usuario.setVetado(usuarioActualizado.getVetado());
            }
            if (usuarioActualizado.getMotivoVeto() != null) {
                usuario.setMotivoVeto(usuarioActualizado.getMotivoVeto());
            }
            if (usuarioActualizado.getFechaVeto() != null) {
                usuario.setFechaVeto(usuarioActualizado.getFechaVeto());
            }
            return usuarioRepositorio.save(usuario);
        }).orElse(null);
    }

    @Transactional
    public boolean confirmarEmail(String token) {
        Optional<UsuarioEntidad> usuarioOpt = usuarioRepositorio.findByCodigoVerificacion(token);
        if (usuarioOpt.isPresent()) {
            UsuarioEntidad usuario = usuarioOpt.get();
            usuario.setActivo(true);
            usuario.setCodigoVerificacion(null);
            usuarioRepositorio.save(usuario);
            return true;
        }
        return false;
    }

    @Transactional
    public String generarTokenRecuperacion(String email) {
        Optional<UsuarioEntidad> usuarioOpt = usuarioRepositorio.findByEmail(email);
        if (usuarioOpt.isPresent()) {
            UsuarioEntidad usuario = usuarioOpt.get();
            // Generar código de 6 dígitos
            String token = String.valueOf((int) ((Math.random() * 900000) + 100000));
            // Usamos codigoVerificacion para el código de recuperación (campo único)
            usuario.setCodigoVerificacion(token);
            usuarioRepositorio.save(usuario);
            return token;
        }
        return null;
    }

    @Transactional
    public boolean restablecerPassword(String token, String nuevaPasswordEncriptada) {
        // Buscar por codigoVerificacion (campo único)
        Optional<UsuarioEntidad> usuarioOpt = usuarioRepositorio.findByCodigoVerificacion(token);
        if (usuarioOpt.isPresent()) {
            UsuarioEntidad usuario = usuarioOpt.get();
            // La nueva contraseña ya viene encriptada del backend web
            usuario.setPassword(nuevaPasswordEncriptada);
            usuario.setCodigoVerificacion(null); // Limpiar código tras uso
            usuarioRepositorio.save(usuario);
            return true;
        }
        return false;
    }

    @Transactional
    public boolean vetarUsuario(Integer id, String motivo) {
        if (id == null)
            return false;
        return usuarioRepositorio.findById(id).map(usuario -> {
            usuario.setVetado(true);
            usuario.setMotivoVeto(motivo);
            usuario.setFechaVeto(LocalDateTime.now());
            usuario.setActivo(false);
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
            UsuarioEntidad usuario = usuarioRepositorio.findById(id).get();
            // PROTECCIÓN OWNER
            if (usuario.getEmail().equalsIgnoreCase("antoniowebserver@gmail.com") ||
                    (usuario.getRol() != null && "Owner".equals(usuario.getRol().getNombre()))) {
                System.out.println("❌ INTENTO DE ELIMINAR AL OWNER BLOQUEADO.");
                return false;
            }
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

    // ==================== GESTIÓN DE ROLES Y OWNER ====================

    @org.springframework.beans.factory.annotation.Autowired
    private com.noticias.api.repositorios.RolRepositorio rolRepositorio;

    @jakarta.annotation.PostConstruct
    public void inicializarRolesYOwner() {
        // 1. Garantizar Roles
        List<String> rolesNecesarios = List.of("Owner", "Admin", "Trabajador", "Usuario");
        for (String nombreRol : rolesNecesarios) {
            if (rolRepositorio.findByNombre(nombreRol).isEmpty()) {
                com.noticias.api.entidades.RolEntidad rol = new com.noticias.api.entidades.RolEntidad();
                rol.setNombre(nombreRol);
                rol.setDescripcion("Rol de sistema " + nombreRol);
                rolRepositorio.save(rol);
            }
        }

        // 2. Garantizar Owner
        garantizarRolOwner();
    }

    private void garantizarRolOwner() {
        String emailOwner = "antoniowebserver@gmail.com";
        Optional<UsuarioEntidad> ownerOpt = usuarioRepositorio.findByEmail(emailOwner);
        com.noticias.api.entidades.RolEntidad rolOwner = rolRepositorio.findByNombre("Owner")
                .orElseThrow(() -> new RuntimeException("Error crítico: Rol Owner no encontrado"));

        if (ownerOpt.isPresent()) {
            UsuarioEntidad owner = ownerOpt.get();
            if (!owner.getRol().getNombre().equals("Owner") || !owner.getEsSuperAdmin()) {
                owner.setRol(rolOwner);
                owner.setEsSuperAdmin(true); // Flag legacy/extra
                owner.setVetado(false); // Owner nunca vetado
                usuarioRepositorio.save(owner);
                System.out.println("✅ Rol Owner asignado a usuario existente: " + emailOwner);
            }
        } else {
            // Crear usuario Owner si no existe (opcional, o esperar registro)
            // Si el usuario se registra despues, deberiamos asignarle el rol.
            // Mejor: Lo dejamos pendiente o creamos un placeholder?
            // El prompt dice "solo le pertenecera a la cuenta...". Si la cuenta no existe,
            // no podemos asignarlo aun.
            // Pero podríamos capturar el registro de este email.
            System.out.println("ℹ️ Usuario Owner (" + emailOwner + ") no existe aún. Se asignará al registrarse.");
        }
    }
}
