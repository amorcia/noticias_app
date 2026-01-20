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

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminControlador {

    @Autowired
    private ApiNoticiasCliente apiCliente;

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

        try {
            List<UsuarioDTO> todosUsuarios = apiCliente.listarUsuarios();
            List<Map<String, Object>> sanciones = apiCliente.listarSanciones();
            List<UsuarioDTO> vetados = apiCliente.listarVetados();
            List<com.noticias.web.dtos.NoticiaEliminadaDTO> noticiasEliminadas = apiCliente.listarNoticiasEliminadas();
            List<DenunciaDTO> denuncias = apiCliente.listarDenuncias();
            Map<String, Object> stats = apiCliente.getAdminStats();

            model.addAttribute("usuarios", todosUsuarios);
            model.addAttribute("sanciones", sanciones);
            model.addAttribute("vetados", vetados);
            model.addAttribute("noticiasEliminadas", noticiasEliminadas);
            model.addAttribute("denuncias", denuncias);
            model.addAttribute("stats", stats);
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Error al conectar con el servicio de administración. " + e.getMessage());
            model.addAttribute("sanciones", List.of());
            model.addAttribute("vetados", List.of());
        }

        return "vistas/admin/PanelAdmin";
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
            HttpSession session) {
        UsuarioDTO admin = (UsuarioDTO) session.getAttribute("usuario");
        if (admin == null || (!"ADMIN".equalsIgnoreCase(admin.getRolNombre())
                && !"OWNER".equalsIgnoreCase(admin.getRolNombre()))) {
            return org.springframework.http.ResponseEntity.status(403).body("No tienes permisos");
        }
        boolean exito = apiCliente.vetarUsuario(id, motivo);
        return exito ? org.springframework.http.ResponseEntity.ok().build()
                : org.springframework.http.ResponseEntity.status(500).build();
    }

    @PostMapping("/usuarios/{id}/desvetar")
    @ResponseBody
    public org.springframework.http.ResponseEntity<?> desvetarUsuario(@PathVariable Integer id, HttpSession session) {
        UsuarioDTO admin = (UsuarioDTO) session.getAttribute("usuario");
        if (admin == null || (!"ADMIN".equalsIgnoreCase(admin.getRolNombre())
                && !"OWNER".equalsIgnoreCase(admin.getRolNombre()))) {
            return org.springframework.http.ResponseEntity.status(403).body("No tienes permisos");
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
        String rol = admin != null ? admin.getRolNombre() : null;
        boolean esOwner = "OWNER".equalsIgnoreCase(rol);

        if (admin == null || !esOwner) {
            return ResponseEntity.status(403).body("Solo el OWNER puede cambiar roles");
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
        UsuarioDTO admin = (UsuarioDTO) session.getAttribute("usuario");
        String rol = admin != null ? admin.getRolNombre() : null;
        boolean esOwner = "OWNER".equalsIgnoreCase(rol);

        if (admin == null || !esOwner) {
            return ResponseEntity.status(403).body("Solo el OWNER puede eliminar usuarios");
        }

        try {
            apiCliente.eliminarUsuario(id);
            return ResponseEntity.ok(Map.of("mensaje", "Usuario eliminado"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
}
