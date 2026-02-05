package com.noticias.api.servicios;

import com.noticias.api.entidades.UsuarioEntidad;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Servicio centralizado para validación de privilegios jerárquicos
 */
@Service
public class PrivilegiosServicio {

    // Jerarquía de roles (menor número = mayor privilegio)
    private static final Map<String, Integer> JERARQUIA_ROLES = new HashMap<>();

    static {
        JERARQUIA_ROLES.put("OWNER", 1);
        JERARQUIA_ROLES.put("ADMIN", 2);
        JERARQUIA_ROLES.put("TRABAJADOR", 3);
        JERARQUIA_ROLES.put("USER", 4);
    }

    // Protección por ROL, no por ID
    private static final String ROL_OWNER = "OWNER";
    private static final String ROL_ADMIN = "ADMIN";
    private static final String ROL_TRABAJADOR = "TRABAJADOR";

    /**
     * Verifica si un usuario puede modificar a otro usuario
     */
    public boolean puedeModificarUsuario(UsuarioEntidad actor, UsuarioEntidad objetivo) {
        if (actor == null || objetivo == null) {
            return false;
        }

        // Nadie puede modificar a un OWNER (excepto el sistema o lógica interna si
        // fuera necesaria, pero aquí no)
        if (esOwnerPorRol(objetivo)) {
            return false;
        }

        int nivelActor = getNivelJerarquico(obtenerNombreRol(actor));
        int nivelObjetivo = getNivelJerarquico(obtenerNombreRol(objetivo));

        // Un usuario solo puede modificar a alguien con nivel ESTRICTAMENTE INFERIOR
        // (número mayor)
        // Ejemplo: OWNER (1) < ADMIN (2) -> TRUE
        // Ejemplo: ADMIN (2) < ADMIN (2) -> FALSE (Pares protegidos)
        return nivelActor < nivelObjetivo;
    }

    /**
     * Verifica si un usuario puede vetar a otro
     */
    public boolean puedeVetarUsuario(UsuarioEntidad actor, UsuarioEntidad objetivo) {
        if (actor == null || objetivo == null) {
            return false;
        }

        // Nadie puede vetar a un OWNER
        if (esOwnerPorRol(objetivo)) {
            return false;
        }

        int nivelActor = getNivelJerarquico(obtenerNombreRol(actor));
        int nivelObjetivo = getNivelJerarquico(obtenerNombreRol(objetivo));

        // Solo permitir acciones sobre niveles inferiores
        return nivelActor < nivelObjetivo;
    }

    /**
     * Verifica si un usuario puede cambiar el rol de otro
     */
    public boolean puedeCambiarRol(UsuarioEntidad actor, String rolActualObjetivo, String nuevoRol) {
        if (actor == null || rolActualObjetivo == null || nuevoRol == null) {
            return false;
        }

        int nivelActor = getNivelJerarquico(obtenerNombreRol(actor));
        int nivelObjetivoActual = getNivelJerarquico(rolActualObjetivo);
        int nivelNuevoRol = getNivelJerarquico(nuevoRol);

        // Nadie puede asignar el rol OWNER (excepto tal vez el sistema, pero no por
        // API)
        if (ROL_OWNER.equalsIgnoreCase(nuevoRol)) {
            return false;
        }

        // Nadie puede modificar a un OWNER
        if (ROL_OWNER.equalsIgnoreCase(rolActualObjetivo)) {
            return false;
        }

        // El actor debe ser de nivel superior al objetivo actual Y al nuevo rol
        // Ejemplo: ADMIN (2) quiere cambiar a USER (4) a TRABAJADOR (3) -> OK (2 < 4
        // and 2 < 3)
        // Ejemplo: ADMIN (2) quiere cambiar a USER (4) a ADMIN (2) -> FAIL (2 is not <
        // 2)
        return nivelActor < nivelObjetivoActual && nivelActor < nivelNuevoRol;
    }

    /**
     * Verifica si un usuario puede eliminar a otro
     */
    public boolean puedeEliminarUsuario(UsuarioEntidad actor, UsuarioEntidad objetivo) {
        if (actor == null || objetivo == null) {
            return false;
        }

        // Nadie puede eliminar a un OWNER
        if (esOwnerPorRol(objetivo)) {
            return false;
        }

        int nivelActor = getNivelJerarquico(obtenerNombreRol(actor));
        int nivelObjetivo = getNivelJerarquico(obtenerNombreRol(objetivo));

        // Solo niveles superiores pueden eliminar inferiores. Peers están protegidos.
        return nivelActor < nivelObjetivo;
    }

    /**
     * Verifica si un usuario es OWNER por su rol
     */
    public boolean esOwnerPorRol(UsuarioEntidad usuario) {
        if (usuario == null) {
            return false;
        }
        String rol = obtenerNombreRol(usuario);
        return ROL_OWNER.equalsIgnoreCase(rol);
    }

    /**
     * Verifica si un usuario tiene acceso al panel de administración
     * TRABAJADOR NO tiene acceso
     */
    public boolean tieneAccesoAdminPanel(UsuarioEntidad usuario) {
        if (usuario == null) {
            return false;
        }
        String rol = obtenerNombreRol(usuario);
        return ROL_OWNER.equalsIgnoreCase(rol) || ROL_ADMIN.equalsIgnoreCase(rol);
    }

    /**
     * Obtiene el nivel jerárquico de un rol
     */
    public int getNivelJerarquico(String rol) {
        return JERARQUIA_ROLES.getOrDefault(rol != null ? rol.toUpperCase() : "USER", 4);
    }

    /**
     * Obtiene el nombre del rol de un usuario de forma segura
     */
    private String obtenerNombreRol(UsuarioEntidad usuario) {
        if (usuario == null || usuario.getRol() == null) {
            return "USER";
        }
        return usuario.getRol().getNombre();
    }
}
