package com.noticias.web.controladores;

import com.noticias.web.dtos.DenunciaDTO;
import com.noticias.web.dtos.UsuarioDTO;
import com.noticias.web.servicios.ApiNoticiasCliente;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
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
    public String resolverSancion(@PathVariable Integer id,
            @RequestParam String resolucion,
            @RequestParam String accion,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        UsuarioDTO admin = (UsuarioDTO) session.getAttribute("usuario");
        String rol = admin != null ? admin.getRolNombre() : null;
        boolean esAdmin = "ADMIN".equalsIgnoreCase(rol) || "OWNER".equalsIgnoreCase(rol);

        if (admin == null || !esAdmin) {
            return "redirect:/auth/login";
        }

        try {
            apiCliente.resolverSancion(id, resolucion, accion, admin.getId());
            redirectAttributes.addFlashAttribute("mensaje", "Sanción resuelta correctamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al resolver sanción.");
        }
        return "redirect:/admin/panel";
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
            return org.springframework.http.ResponseEntity.status(403).body("No tienes permisos");
        }
        boolean exito = apiCliente.resolverDenuncia(id, estado);
        return exito ? org.springframework.http.ResponseEntity.ok().build()
                : org.springframework.http.ResponseEntity.status(500).build();
    }
}
