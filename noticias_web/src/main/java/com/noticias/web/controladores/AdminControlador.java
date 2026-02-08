package com.noticias.web.controladores;

import com.noticias.web.dtos.DenunciaDTO;
import com.noticias.web.dtos.SancionDTO;
import com.noticias.web.dtos.UsuarioDTO;
import com.noticias.web.servicios.ApiNoticiasCliente;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import org.springframework.http.MediaType;

@Controller
@RequestMapping("/admin")
public class AdminControlador {

    @Autowired
    private ApiNoticiasCliente apiCliente;

    @Autowired
    private com.noticias.web.servicios.ExportacionServicio exportacionServicio;

    @Autowired
    private com.noticias.web.servicios.LoggerService logger; // Inject Logger

    /**
     * @author amorcia
     *         METODO - Helper para calcular nivel jerárquico del rol
     */
    private Integer calcularNivel(String rol) {
        if (rol == null)
            return 4;
        String r = rol.toUpperCase();
        if (r.contains("OWNER"))
            return 1;
        if (r.contains("ADMIN"))
            return 2;
        if (r.contains("TRABAJADOR"))
            return 3;
        return 4;
    }

    /**
     * @author amorcia
     *         METODO - Helper para validar jerarquía entre usuarios
     * @return true si el actor tiene mayor rango (menor nivel) que el objetivo
     */
    private boolean validarJerarquiaSuperior(UsuarioDTO actor, UsuarioDTO objetivo) {
        if (actor.getRolNivel() == null)
            actor.setRolNivel(calcularNivel(actor.getRolNombre()));
        if (objetivo.getRolNivel() == null)
            objetivo.setRolNivel(calcularNivel(objetivo.getRolNombre()));
        return actor.getRolNivel() < objetivo.getRolNivel();
    }

    /**
     * @author amorcia
     *         METODO - Helper para sanitizar lista de usuarios para la vista
     */
    private void sanitizarUsuarios(List<UsuarioDTO> usuarios) {
        if (usuarios == null)
            return;
        for (UsuarioDTO u : usuarios) {
            if (u.getRolNombre() == null)
                u.setRolNombre("USER");
            if (u.getRolId() == null)
                u.setRolId(4);
            u.setRolNivel(calcularNivel(u.getRolNombre()));
            if (u.getNombreCompleto() == null || u.getNombreCompleto().trim().isEmpty())
                u.setNombreCompleto("Usuario Sin Nombre");
            if (u.getEmail() == null || u.getEmail().trim().isEmpty())
                u.setEmail("sin_email@sistema.local");
            if (u.getVetado() == null)
                u.setVetado(false);
            if (u.getMovil() == null || u.getMovil().trim().isEmpty())
                u.setMovil("N/A");
            if (u.getImagenUrl() == null)
                u.setImagenUrl("");
        }
    }

    /**
     * @author amorcia
     *         METODO - Helper para sanitizar noticias eliminadas para la vista
     */
    private void sanitizarNoticiasEliminadas(List<com.noticias.web.dtos.NoticiaEliminadaDTO> noticias) {
        if (noticias == null)
            return;
        for (com.noticias.web.dtos.NoticiaEliminadaDTO ne : noticias) {
            if (ne.getFechaEliminacion() == null)
                ne.setFechaEliminacion(java.time.LocalDateTime.now());
            if (ne.getTitulo() == null)
                ne.setTitulo("Sin título");
            if (ne.getMotivo() == null)
                ne.setMotivo("Desconocido");
            if (ne.getDescripcion() == null)
                ne.setDescripcion("No disponible");
            if (ne.getEliminadoPorNombre() == null)
                ne.setEliminadoPorNombre("Sistema");
            if (ne.getRolEliminador() == null)
                ne.setRolEliminador("ADMIN");
            if (ne.getCategoriaColor() == null)
                ne.setCategoriaColor("#888888");
            if (ne.getCategoriaNombre() == null)
                ne.setCategoriaNombre("Desconocida");
        }
    }

    /**
     * @author amorcia
     *         METODO - Muestra el panel de administración con estadísticas y listas
     * @param model   Modelo
     * @param session Sesión
     * @return Vista del panel
     */
    @GetMapping("/panel")
    public String panel(Model model, HttpSession session) {
        System.out.println("[ADMIN DEBUG] Requesting /admin/panel");
        UsuarioDTO usuario = (UsuarioDTO) session.getAttribute("usuario");

        if (usuario == null) {
            System.out.println("[ADMIN DEBUG] No user in session. Redirecting to login.");
            return "redirect:/auth/login";
        }

        System.out.println("[ADMIN DEBUG] User found: " + usuario.getEmail() + " | Role: " + usuario.getRolNombre());

        String rol = usuario.getRolNombre();
        // Check ALL acceptable variants manually
        boolean esAdmin = "ADMIN".equalsIgnoreCase(rol) || "OWNER".equalsIgnoreCase(rol)
                || "Admin".equalsIgnoreCase(rol) || "Owner".equalsIgnoreCase(rol);

        if (!esAdmin) {
            System.out.println("[ADMIN DEBUG] Access Denied. Role not sufficient.");
            return "redirect:/error/403";
        }

        logger.logAction(usuario.getEmail(), "ACCESS_ADMIN_PANEL", "Has accedido al panel de administración");

        // Initialize defaults
        model.addAttribute("usuarios", java.util.Collections.emptyList());
        model.addAttribute("sanciones", java.util.Collections.emptyList());
        model.addAttribute("vetados", java.util.Collections.emptyList());
        model.addAttribute("noticiasEliminadas", java.util.Collections.emptyList());
        model.addAttribute("denuncias", java.util.Collections.emptyList());

        // stats using DTO
        com.noticias.web.dtos.AdminStatsDTO safeStats = new com.noticias.web.dtos.AdminStatsDTO();
        model.addAttribute("stats", safeStats);

        try {
            System.out.println("[ADMIN DEBUG] Starting data fetch...");
            long startTotal = System.currentTimeMillis();

            // Fix: Calculate level if missing
            if (usuario.getRolNivel() == null) {
                usuario.setRolNivel(calcularNivel(usuario.getRolNombre()));
                System.out.println("[ADMIN DEBUG] Calculated Role Level: " + usuario.getRolNivel());
            }

            System.out.println("[ADMIN DEBUG] Fetching Users...");
            long subStart = System.currentTimeMillis();
            List<UsuarioDTO> todosUsuarios = apiCliente.listarUsuarios();
            System.out.println("[ADMIN DEBUG] Users Fetched in " + (System.currentTimeMillis() - subStart)
                    + "ms. Count: " + (todosUsuarios != null ? todosUsuarios.size() : "null"));

            if (todosUsuarios == null) {
                System.out.println("[ADMIN DEBUG] WARNING: Users list is NULL from API");
            } else if (todosUsuarios.isEmpty()) {
                System.out.println("[ADMIN DEBUG] WARNING: Users list is EMPTY from API");
            }

            System.out.println("[ADMIN DEBUG] Sanitizing Users...");
            sanitizarUsuarios(todosUsuarios);
            System.out.println("[ADMIN DEBUG] Sanitization complete.");

            System.out.println("[ADMIN DEBUG] Fetching Sanctions...");
            subStart = System.currentTimeMillis();
            List<SancionDTO> sanciones = apiCliente.listarSanciones();
            System.out.println("[ADMIN DEBUG] Sanctions Fetched in " + (System.currentTimeMillis() - subStart) + "ms.");

            System.out.println("[ADMIN DEBUG] Fetching Banned Users...");
            subStart = System.currentTimeMillis();
            List<UsuarioDTO> vetados = apiCliente.listarVetados(); // Vetados should be UsuarioDTO
            if (vetados != null)
                sanitizarUsuarios(vetados);
            System.out.println(
                    "[ADMIN DEBUG] Banned Users Fetched in " + (System.currentTimeMillis() - subStart) + "ms.");

            System.out.println("[ADMIN DEBUG] Fetching Deleted News...");
            subStart = System.currentTimeMillis();
            List<com.noticias.web.dtos.NoticiaEliminadaDTO> noticiasEliminadas = apiCliente.listarNoticiasEliminadas();
            sanitizarNoticiasEliminadas(noticiasEliminadas);
            System.out.println(
                    "[ADMIN DEBUG] Deleted News Fetched in " + (System.currentTimeMillis() - subStart) + "ms.");

            System.out.println("[ADMIN DEBUG] Fetching Reports...");
            subStart = System.currentTimeMillis();
            List<DenunciaDTO> denuncias = apiCliente.listarDenuncias();
            System.out.println("[ADMIN DEBUG] Reports Fetched in " + (System.currentTimeMillis() - subStart) + "ms.");

            System.out.println("[ADMIN DEBUG] Fetching Stats...");
            subStart = System.currentTimeMillis();
            Map<String, Object> rawStats = apiCliente.getAdminStats();
            System.out.println("[ADMIN DEBUG] Stats Raw Response: " + rawStats);
            if (rawStats != null) {
                model.addAttribute("stats", new com.noticias.web.dtos.AdminStatsDTO(rawStats));
            } else {
                System.out.println("[ADMIN DEBUG] WARNING: Stats are NULL from API");
            }
            System.out.println(
                    "[ADMIN DEBUG] Stats Fetched and Processed in " + (System.currentTimeMillis() - subStart) + "ms.");

            if (todosUsuarios != null)
                model.addAttribute("usuarios", todosUsuarios);
            if (sanciones != null)
                model.addAttribute("sanciones", sanciones);
            if (vetados != null)
                model.addAttribute("vetados", vetados);
            if (noticiasEliminadas != null)
                model.addAttribute("noticiasEliminadas", noticiasEliminadas);
            if (denuncias != null)
                model.addAttribute("denuncias", denuncias);

            System.out.println("[ADMIN DEBUG] Data fetch complete in " + (System.currentTimeMillis() - startTotal)
                    + "ms. Rendering view.");

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("❌ CRITICAL ERROR in AdminControlador: " + e.getMessage());
            model.addAttribute("error", "Error crítico en panel: " + e.getMessage());
            // Do NOT rethrow, allow page to load with empty tables so we can see the UI
        }

        return "vistas/admin/PanelAdmin";
    }

    /**
     * @author amorcia
     *         METODO - Resuelve una sanción aplicada
     */
    @PostMapping("/sanciones/{id}/resolver")
    public ResponseEntity<?> resolverSancion(@PathVariable Integer id,
            @RequestParam String resolucion,
            @RequestParam String accion,
            @RequestHeader(value = "X-Requested-With", required = false) String requestedWith,
            HttpSession session) {
        UsuarioDTO admin = (UsuarioDTO) session.getAttribute("usuario");
        String rol = admin != null ? admin.getRolNombre() : null;
        boolean esAdmin = "ADMIN".equalsIgnoreCase(rol) || "OWNER".equalsIgnoreCase(rol);

        if (admin == null || !esAdmin) {
            if ("XMLHttpRequest".equals(requestedWith)) {
                return ResponseEntity.status(403).body("No tienes permisos de administrador");
            }
            return ResponseEntity.status(302).header("Location", "/auth/login").build();
        }

        try {
            apiCliente.resolverSancion(id, resolucion, accion, admin.getId());
            if ("XMLHttpRequest".equals(requestedWith)) {
                return ResponseEntity.ok(Map.of("mensaje", "Sanción resuelta"));
            }
            return ResponseEntity.status(302).header("Location", "/admin/panel?success=sancion").build();
        } catch (Exception e) {
            if ("XMLHttpRequest".equals(requestedWith)) {
                return ResponseEntity.status(500).body("Error al resolver la sanción: " + e.getMessage());
            }
            return ResponseEntity.status(302).header("Location", "/admin/panel?error=sancion").build();
        }
    }

    /**
     * @author amorcia
     *         METODO - Veta a un usuario (prohibe acceso)
     */
    @PostMapping("/usuarios/{id}/vetar")
    @ResponseBody
    public org.springframework.http.ResponseEntity<?> vetarUsuario(@PathVariable Integer id,
            @RequestParam String motivo,
            @RequestParam(required = false) String duracion,
            HttpSession session) {
        UsuarioDTO admin = (UsuarioDTO) session.getAttribute("usuario");
        if (admin == null)
            return ResponseEntity.status(401).build();

        UsuarioDTO target = apiCliente.buscarUsuarioPorId(id);
        if (target == null)
            return ResponseEntity.notFound().build();

        if (!validarJerarquiaSuperior(admin, target)) {
            return org.springframework.http.ResponseEntity.status(403)
                    .body("No tienes permisos para vetar a este usuario (Mismo nivel o superior)");
        }

        boolean exito = apiCliente.vetarUsuario(id, motivo, duracion);
        return exito ? org.springframework.http.ResponseEntity.ok().build()
                : org.springframework.http.ResponseEntity.status(500).build();
    }

    /**
     * @author amorcia
     *         METODO - Levanta el veto de un usuario
     */
    @PostMapping("/usuarios/{id}/desvetar")
    @ResponseBody
    public org.springframework.http.ResponseEntity<?> desvetarUsuario(@PathVariable Integer id, HttpSession session) {
        UsuarioDTO admin = (UsuarioDTO) session.getAttribute("usuario");
        if (admin == null)
            return ResponseEntity.status(401).build();

        UsuarioDTO target = apiCliente.buscarUsuarioPorId(id);
        if (target == null)
            return ResponseEntity.notFound().build();

        if (!validarJerarquiaSuperior(admin, target)) {
            return org.springframework.http.ResponseEntity.status(403).body("No tienes permisos sobre este usuario");
        }

        boolean exito = apiCliente.desvetarUsuario(id);
        return exito ? org.springframework.http.ResponseEntity.ok().build()
                : org.springframework.http.ResponseEntity.status(500).build();
    }

    /**
     * @author amorcia
     *         METODO - Resuelve una denuncia
     */
    @PostMapping("/denuncias/{id}/resolver")
    @ResponseBody
    public org.springframework.http.ResponseEntity<?> resolverDenuncia(@PathVariable Integer id,
            @RequestParam String estado, HttpSession session) {
        UsuarioDTO admin = (UsuarioDTO) session.getAttribute("usuario");
        if (admin == null || (!"ADMIN".equalsIgnoreCase(admin.getRolNombre())
                && !"OWNER".equalsIgnoreCase(admin.getRolNombre()))) {
            return ResponseEntity.status(403).body("No tienes permisos");
        }
        boolean exito = apiCliente.resolverDenuncia(id, estado);
        return exito ? ResponseEntity.ok().build()
                : ResponseEntity.status(500).build();
    }

    /**
     * @author amorcia
     *         METODO - Cambia el rol de un usuario
     */
    @PostMapping("/usuarios/{id}/rol")
    @ResponseBody
    public ResponseEntity<?> cambiarRol(@PathVariable Integer id, @RequestParam Integer rolId, HttpSession session) {
        UsuarioDTO admin = (UsuarioDTO) session.getAttribute("usuario");
        if (admin == null)
            return ResponseEntity.status(401).build();

        UsuarioDTO target = apiCliente.buscarUsuarioPorId(id);
        if (target == null)
            return ResponseEntity.notFound().build();

        if (!validarJerarquiaSuperior(admin, target)) {
            return ResponseEntity.status(403).body("No tienes permisos para modificar este usuario");
        }

        if (admin.getRolNivel() >= rolId) {
            return ResponseEntity.status(403).body("No puedes asignar un rol igual o superior al tuyo");
        }

        try {
            UsuarioDTO u = new UsuarioDTO();
            u.setRolId(rolId);
            apiCliente.actualizarUsuario(id, u);
            return ResponseEntity.ok(Map.of("mensaje", "Rol actualizado"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    /**
     * @author amorcia
     *         METODO - Elimina usuario de forma permanente (placeholder)
     */
    @DeleteMapping("/usuarios/{id}")
    @ResponseBody
    public ResponseEntity<?> eliminarUsuario(@PathVariable Integer id, HttpSession session) {
        return ResponseEntity.status(405).body("Use POST /eliminar-con-justificacion");
    }

    /**
     * @author amorcia
     *         METODO - Elimina usuario con motivo y descripción
     */
    @PostMapping("/usuarios/{id}/eliminar-con-justificacion")
    @ResponseBody
    public ResponseEntity<?> eliminarUsuarioConJustificacion(@PathVariable Integer id,
            @RequestParam String motivo,
            @RequestParam String descripcion,
            HttpSession session) {
        UsuarioDTO admin = (UsuarioDTO) session.getAttribute("usuario");
        if (admin == null)
            return ResponseEntity.status(401).build();

        UsuarioDTO target = apiCliente.buscarUsuarioPorId(id);
        if (target == null)
            return ResponseEntity.notFound().build();

        if (!validarJerarquiaSuperior(admin, target)) {
            return ResponseEntity.status(403).body("No tienes permisos para eliminar a este usuario");
        }

        try {
            apiCliente.eliminarUsuarioConJustificacion(id, motivo, descripcion, admin.getId());
            return ResponseEntity.ok(Map.of("mensaje", "Usuario eliminado con justificación"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    /**
     * @author amorcia
     *         METODO - Genera y descarga reporte en PDF
     */
    @GetMapping("/exportar-pdf")
    public ResponseEntity<byte[]> exportarPdf(HttpSession session) {
        UsuarioDTO admin = (UsuarioDTO) session.getAttribute("usuario");
        String rol = admin != null ? admin.getRolNombre() : null;
        boolean esAdmin = "ADMIN".equalsIgnoreCase(rol) || "OWNER".equalsIgnoreCase(rol);

        if (admin == null || !esAdmin) {
            return ResponseEntity.status(403).build();
        }

        try {
            byte[] pdfBytes = exportacionServicio.generarReporteCompleto();

            if (pdfBytes == null || pdfBytes.length == 0) {
                System.err.println("Error: PDF generado está vacío");
                return ResponseEntity.status(500).body(null);
            }

            return ResponseEntity.ok()
                    .header("Content-Disposition",
                            "attachment; filename=reporte_sistema_" + java.time.LocalDate.now() + ".pdf")
                    .contentType(org.springframework.http.MediaType.APPLICATION_PDF)
                    .body(pdfBytes);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }
}
