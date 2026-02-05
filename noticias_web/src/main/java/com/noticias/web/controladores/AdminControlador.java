package com.noticias.web.controladores;

import com.noticias.web.dtos.DenunciaDTO;
import com.noticias.web.dtos.UsuarioDTO;
import com.noticias.web.servicios.ApiNoticiasCliente;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
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

    @GetMapping("/panel")
    public String panel(Model model, HttpSession session) {
        UsuarioDTO usuario = (UsuarioDTO) session.getAttribute("usuario");

        // 1. No logueado -> Login
        if (usuario == null) {
            return "redirect:/auth/login";
        }

        // 2. Logueado pero sin permisos -> 403
        String rol = usuario.getRolNombre();
        boolean esAdmin = "ADMIN".equalsIgnoreCase(rol) || "OWNER".equalsIgnoreCase(rol)
                || "Admin".equalsIgnoreCase(rol) || "Owner".equalsIgnoreCase(rol);

        if (!esAdmin) {
            return "redirect:/error/403";
        }

        // Initialize lists with safe defaults
        model.addAttribute("usuarios", List.of());
        model.addAttribute("sanciones", List.of());
        model.addAttribute("vetados", List.of());
        model.addAttribute("noticiasEliminadas", List.of());
        model.addAttribute("denuncias", List.of());

        // Ensure stats has ALL keys required by chart.js
        Map<String, Object> defaultStats = new HashMap<>();
        defaultStats.put("totalUsuarios", 0);
        defaultStats.put("usuariosVetados", 0);
        defaultStats.put("porcentajeVetados", 0);
        defaultStats.put("totalNoticias", 0);
        defaultStats.put("totalSanciones", 0);
        defaultStats.put("sancionesPendientes", 0);
        model.addAttribute("stats", defaultStats);

        try {
            System.out.println("🔍 AdminControlador: Cargando datos del panel...");

            // Asegurar que el usuario en sesión tiene su nivel
            if (usuario.getRolNivel() == null) {
                usuario.setRolNivel(calcularNivel(usuario.getRolNombre()));
            }

            List<UsuarioDTO> todosUsuarios = apiCliente.listarUsuarios();

            if (todosUsuarios != null) {
                for (UsuarioDTO u : todosUsuarios) {
                    // Sanitize all display fields to prevent Thymeleaf errors
                    if (u.getRolNombre() == null)
                        u.setRolNombre("USER");
                    if (u.getRolId() == null)
                        u.setRolId(4);

                    // Asignar nivel jerárquico
                    u.setRolNivel(calcularNivel(u.getRolNombre()));

                    if (u.getNombreCompleto() == null)
                        u.setNombreCompleto("Usuario Sin Nombre");
                    if (u.getEmail() == null)
                        u.setEmail("sin_email@sistema.local");
                    if (u.getVetado() == null)
                        u.setVetado(false);
                }
            }

            List<Map<String, Object>> sanciones = apiCliente.listarSanciones();
            List<UsuarioDTO> vetados = apiCliente.listarVetados();
            List<com.noticias.web.dtos.NoticiaEliminadaDTO> noticiasEliminadas = apiCliente.listarNoticiasEliminadas();

            if (noticiasEliminadas != null) {
                for (com.noticias.web.dtos.NoticiaEliminadaDTO ne : noticiasEliminadas) {
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

            List<DenunciaDTO> denuncias = apiCliente.listarDenuncias();
            Map<String, Object> stats = apiCliente.getAdminStats();

            model.addAttribute("usuarios", todosUsuarios != null ? todosUsuarios : List.of());
            model.addAttribute("sanciones", sanciones != null ? sanciones : List.of());
            model.addAttribute("vetados", vetados != null ? vetados : List.of());
            model.addAttribute("noticiasEliminadas", noticiasEliminadas != null ? noticiasEliminadas : List.of());
            model.addAttribute("denuncias", denuncias != null ? denuncias : List.of());

            if (stats != null) {
                // Merge with defaults to ensure no missing keys
                Map<String, Object> safeStats = new HashMap<>(defaultStats);
                safeStats.putAll(stats);
                model.addAttribute("stats", safeStats);
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("❌ Error en AdminControlador: " + e.getMessage());
            model.addAttribute("error", "Error al conectar con el servicio de administración. " + e.getMessage());
        }

        return "vistas/admin/PanelAdmin";
    }

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

    @PostMapping("/usuarios/{id}/vetar")
    @ResponseBody
    public org.springframework.http.ResponseEntity<?> vetarUsuario(@PathVariable Integer id,
            @RequestParam String motivo,
            @RequestParam(required = false) String duracion,
            HttpSession session) {
        UsuarioDTO admin = (UsuarioDTO) session.getAttribute("usuario");
        if (admin == null)
            return ResponseEntity.status(401).build();

        // Cargar niveles si no están
        if (admin.getRolNivel() == null)
            admin.setRolNivel(calcularNivel(admin.getRolNombre()));

        UsuarioDTO target = apiCliente.buscarUsuarioPorId(id);
        if (target == null)
            return ResponseEntity.notFound().build();
        target.setRolNivel(calcularNivel(target.getRolNombre()));

        // Validar jerarquía: actor nivel debe ser MENOR (mejor) que objetivo
        if (admin.getRolNivel() >= target.getRolNivel()) {
            return org.springframework.http.ResponseEntity.status(403)
                    .body("No tienes permisos para vetar a este usuario (Mismo nivel o superior)");
        }

        boolean exito = apiCliente.vetarUsuario(id, motivo, duracion);
        return exito ? org.springframework.http.ResponseEntity.ok().build()
                : org.springframework.http.ResponseEntity.status(500).build();
    }

    @PostMapping("/usuarios/{id}/desvetar")
    @ResponseBody
    public org.springframework.http.ResponseEntity<?> desvetarUsuario(@PathVariable Integer id, HttpSession session) {
        UsuarioDTO admin = (UsuarioDTO) session.getAttribute("usuario");
        if (admin == null)
            return ResponseEntity.status(401).build();

        if (admin.getRolNivel() == null)
            admin.setRolNivel(calcularNivel(admin.getRolNombre()));

        UsuarioDTO target = apiCliente.buscarUsuarioPorId(id);
        if (target == null)
            return ResponseEntity.notFound().build();
        target.setRolNivel(calcularNivel(target.getRolNombre()));

        if (admin.getRolNivel() >= target.getRolNivel()) {
            return org.springframework.http.ResponseEntity.status(403).body("No tienes permisos sobre este usuario");
        }

        boolean exito = apiCliente.desvetarUsuario(id);
        return exito ? org.springframework.http.ResponseEntity.ok().build()
                : org.springframework.http.ResponseEntity.status(500).build();
    }

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

    @PostMapping("/usuarios/{id}/rol")
    @ResponseBody
    public ResponseEntity<?> cambiarRol(@PathVariable Integer id, @RequestParam Integer rolId, HttpSession session) {
        UsuarioDTO admin = (UsuarioDTO) session.getAttribute("usuario");
        if (admin == null)
            return ResponseEntity.status(401).build();

        if (admin.getRolNivel() == null)
            admin.setRolNivel(calcularNivel(admin.getRolNombre()));

        UsuarioDTO target = apiCliente.buscarUsuarioPorId(id);
        if (target == null)
            return ResponseEntity.notFound().build();
        target.setRolNivel(calcularNivel(target.getRolNombre()));

        // Solo se puede cambiar rol si actorNivel < targetNivelActual
        if (admin.getRolNivel() >= target.getRolNivel()) {
            return ResponseEntity.status(403).body("No tienes permisos para modificar este usuario");
        }

        // Además, el nuevo rol no puede ser superior o igual al del actor
        // jerarquía: 1:OWNER, 2:ADMIN, 3:TRABAJADOR, 4:USER
        // (rolId: 1:Owner, 2:Admin, 3:Trabajador, 4:User) -> Mismo mapeo que nivel
        // usualmente
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

    @DeleteMapping("/usuarios/{id}")
    @ResponseBody
    public ResponseEntity<?> eliminarUsuario(@PathVariable Integer id, HttpSession session) {
        return ResponseEntity.status(405).body("Use POST /eliminar-con-justificacion");
    }

    @PostMapping("/usuarios/{id}/eliminar-con-justificacion")
    @ResponseBody
    public ResponseEntity<?> eliminarUsuarioConJustificacion(@PathVariable Integer id,
            @RequestParam String motivo,
            @RequestParam String descripcion,
            HttpSession session) {
        UsuarioDTO admin = (UsuarioDTO) session.getAttribute("usuario");
        if (admin == null)
            return ResponseEntity.status(401).build();

        if (admin.getRolNivel() == null)
            admin.setRolNivel(calcularNivel(admin.getRolNombre()));

        UsuarioDTO target = apiCliente.buscarUsuarioPorId(id);
        if (target == null)
            return ResponseEntity.notFound().build();
        target.setRolNivel(calcularNivel(target.getRolNombre()));

        if (admin.getRolNivel() >= target.getRolNivel()) {
            return ResponseEntity.status(403).body("No tienes permisos para eliminar a este usuario");
        }

        try {
            apiCliente.eliminarUsuarioConJustificacion(id, motivo, descripcion, admin.getId());
            return ResponseEntity.ok(Map.of("mensaje", "Usuario eliminado con justificación"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

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
