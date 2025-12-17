/**
 * RBAC - Role-Based Access Control
 * Sistema de control de acceso basado en roles para TaskFlow
 * 
 * Jerarquía de roles (por ID):
 * 1 - Owner (máximo privilegio)
 * 2 - Admin
 * 3 - Jefe de Proyecto
 * 4 - Product Owner
 * 5 - Scrum Master
 * 6 - DV
 * 7 - Colaborador (mínimo privilegio)
 */

const RBAC = (function() {
    'use strict';

    // Definición de roles con sus IDs
    const ROLES = {
        OWNER: { id: 1, name: 'Owner' },
        ADMIN: { id: 2, name: 'Admin' },
        JEFE_PROYECTO: { id: 3, name: 'Jefe de Proyecto' },
        PRODUCT_OWNER: { id: 4, name: 'Product Owner' },
        SCRUM_MASTER: { id: 5, name: 'Scrum Master' },
        DV: { id: 6, name: 'DV' },
        COLABORADOR: { id: 7, name: 'Colaborador' }
    };

    // Lista ordenada de roles (de mayor a menor privilegio)
    const ROLE_LIST = [
        ROLES.OWNER,
        ROLES.ADMIN,
        ROLES.JEFE_PROYECTO,
        ROLES.PRODUCT_OWNER,
        ROLES.SCRUM_MASTER,
        ROLES.DV,
        ROLES.COLABORADOR
    ];

    /**
     * Obtiene el objeto de rol por nombre
     * @param {string} roleName - Nombre del rol
     * @returns {object|null} Objeto de rol o null si no existe
     */
    function getRoleByName(roleName) {
        if (!roleName) return null;
        return ROLE_LIST.find(r => r.name === roleName) || null;
    }

    /**
     * Verifica si un usuario puede acceder a la gestión de usuarios
     * Solo Owner y Admin pueden acceder
     * @param {string} userRole - Rol del usuario actual
     * @returns {boolean}
     */
    function canAccessUserManagement(userRole) {
        const role = getRoleByName(userRole);
        if (!role) return false;
        return role.id === ROLES.OWNER.id || role.id === ROLES.ADMIN.id;
    }

    /**
     * Obtiene los roles que un usuario puede asignar a otros
     * - Owner: puede asignar cualquier rol
     * - Admin: puede asignar roles con ID > 2 (solo roles inferiores a Admin)
     * - Otros: no pueden asignar roles
     * @param {string} userRole - Rol del usuario actual
     * @returns {array} Array de objetos de rol que puede asignar
     */
    function getAssignableRoles(userRole) {
        const role = getRoleByName(userRole);
        if (!role) return [];

        if (role.id === ROLES.OWNER.id) {
            // Owner puede asignar cualquier rol
            return ROLE_LIST;
        } else if (role.id === ROLES.ADMIN.id) {
            // Admin solo puede asignar roles con ID > 2
            return ROLE_LIST.filter(r => r.id > 2);
        }
        
        // Otros roles no pueden asignar
        return [];
    }

    /**
     * Verifica si un usuario puede editar a otro usuario
     * - Owner: puede editar a todos EXCEPTO otros Owners
     * - Admin: puede editar a sí mismo y a usuarios con roles inferiores (ID > 2)
     * - Otros: no pueden editar
     * @param {string} currentUserRole - Rol del usuario actual
     * @param {string} targetUserRole - Rol del usuario objetivo
     * @param {string} currentUserEmail - Email del usuario actual
     * @param {string} targetUserEmail - Email del usuario objetivo
     * @returns {boolean}
     */
    function canEditUser(currentUserRole, targetUserRole, currentUserEmail, targetUserEmail) {
        const currentRole = getRoleByName(currentUserRole);
        const targetRole = getRoleByName(targetUserRole);
        
        if (!currentRole || !targetRole) return false;

        // Owner puede editar a todos excepto otros Owners
        if (currentRole.id === ROLES.OWNER.id) {
            return targetRole.id !== ROLES.OWNER.id;
        }

        // Admin puede editarse a sí mismo
        if (currentRole.id === ROLES.ADMIN.id && currentUserEmail === targetUserEmail) {
            return true;
        }

        // Admin puede editar usuarios con roles inferiores (ID > 2)
        if (currentRole.id === ROLES.ADMIN.id) {
            return targetRole.id > 2;
        }

        return false;
    }

    /**
     * Verifica si un usuario puede eliminar a otro usuario
     * - Owner: puede eliminar a todos EXCEPTO a sí mismo y otros Owners
     * - Admin: puede eliminar solo usuarios con ID > 2 (roles inferiores)
     * - Otros: no pueden eliminar
     * @param {string} currentUserRole - Rol del usuario actual
     * @param {string} targetUserRole - Rol del usuario objetivo
     * @param {string} currentUserEmail - Email del usuario actual
     * @param {string} targetUserEmail - Email del usuario objetivo
     * @returns {boolean}
     */
    function canDeleteUser(currentUserRole, targetUserRole, currentUserEmail, targetUserEmail) {
        const currentRole = getRoleByName(currentUserRole);
        const targetRole = getRoleByName(targetUserRole);
        
        if (!currentRole || !targetRole) return false;

        // No puede eliminarse a sí mismo
        if (currentUserEmail === targetUserEmail) return false;

        // Owner puede eliminar a todos excepto otros Owners
        if (currentRole.id === ROLES.OWNER.id) {
            return targetRole.id !== ROLES.OWNER.id;
        }

        // Admin puede eliminar solo usuarios con roles inferiores (ID > 2)
        if (currentRole.id === ROLES.ADMIN.id) {
            return targetRole.id > 2;
        }

        return false;
    }

    /**
     * Puebla un select con las opciones de rol según los permisos del usuario
     * @param {string} selectId - ID del elemento select
     * @param {string} userRole - Rol del usuario actual
     */
    function populateRoleSelect(selectId, userRole) {
        const select = document.getElementById(selectId);
        if (!select) {
            console.error(`[RBAC] Select con ID '${selectId}' no encontrado`);
            return;
        }

        // Limpiar opciones existentes
        select.innerHTML = '';

        // Obtener roles asignables
        const assignableRoles = getAssignableRoles(userRole);

        if (assignableRoles.length === 0) {
            console.warn(`[RBAC] Usuario con rol '${userRole}' no puede asignar roles`);
            const option = document.createElement('option');
            option.value = '';
            option.textContent = 'Sin permisos para asignar roles';
            option.disabled = true;
            select.appendChild(option);
            return;
        }

        // Añadir opciones
        assignableRoles.forEach(role => {
            const option = document.createElement('option');
            option.value = role.name;
            option.textContent = role.name;
            select.appendChild(option);
        });

        console.log(`[RBAC] Select '${selectId}' poblado con ${assignableRoles.length} roles para usuario '${userRole}'`);
    }

    /**
     * Oculta/muestra elementos según permisos de acceso
     * @param {string} userRole - Rol del usuario actual
     */
    function applyAccessControl(userRole) {
        // Ocultar botón de gestión de usuarios si no tiene permisos
        const btnGestionUsuarios = document.getElementById('btn-gestion-usuarios');
        if (btnGestionUsuarios) {
            if (!canAccessUserManagement(userRole)) {
                btnGestionUsuarios.style.display = 'none';
                console.log(`[RBAC] Botón 'Gestionar Usuarios' ocultado para rol '${userRole}'`);
            } else {
                btnGestionUsuarios.style.display = '';
                console.log(`[RBAC] Botón 'Gestionar Usuarios' visible para rol '${userRole}'`);
            }
        }
    }

    // API pública
    return {
        canAccessUserManagement,
        getAssignableRoles,
        canEditUser,
        canDeleteUser,
        populateRoleSelect,
        applyAccessControl,
        ROLES
    };
})();

// Log de inicialización
console.log('[RBAC] Sistema de control de acceso basado en roles inicializado');
