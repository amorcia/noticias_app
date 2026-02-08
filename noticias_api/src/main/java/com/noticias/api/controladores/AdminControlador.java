package com.noticias.api.controladores;

import com.noticias.api.entidades.SancionEntidad;
import com.noticias.api.entidades.UsuarioEntidad;
import com.noticias.api.entidades.DenunciaEntidad;
import com.noticias.api.repositorios.SancionRepositorio;
import com.noticias.api.repositorios.UsuarioRepositorio;
import com.noticias.api.repositorios.DenunciaRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/admin")
@CrossOrigin(origins = "*")
public class AdminControlador {

    @Autowired
    private UsuarioRepositorio usuarioRepositorio;

    @Autowired
    private DenunciaRepositorio denunciaRepositorio;

    @Autowired
    private SancionRepositorio sancionRepositorio;

    @Autowired
    private com.noticias.api.repositorios.NoticiaRepositorio noticiaRepositorio;

    @Autowired
    private com.noticias.api.servicios.LoggerService logger; // Inject

    // moderacionServicio eliminada por no usarse

    // --- ESTADÍSTICAS ---
    @GetMapping("/stats")
    public ResponseEntity<java.util.Map<String, Object>> getStats() {
        System.out.println("[API ADMIN] GET /stats called");
        long start = System.currentTimeMillis();
        long totalUsuarios = usuarioRepositorio.count();
        long usuariosVetados = usuarioRepositorio.findByVetadoTrue().size();

        long totalSanciones = sancionRepositorio.count();
        long sancionesPendientes = sancionRepositorio.findByEstado("PENDIENTE").size();
        long totalNoticias = noticiaRepositorio.count();

        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("totalUsuarios", totalUsuarios);
        stats.put("usuariosVetados", usuariosVetados);
        stats.put("totalSanciones", totalSanciones);
        stats.put("sancionesPendientes", sancionesPendientes);
        stats.put("totalNoticias", totalNoticias);

        double porcentajeVetados = totalUsuarios > 0 ? ((double) usuariosVetados / totalUsuarios) * 100 : 0;
        stats.put("porcentajeVetados", Math.round(porcentajeVetados * 100.0) / 100.0);

        System.out.println(
                "[API ADMIN] GET /stats finished in " + (System.currentTimeMillis() - start) + "ms. Stats: " + stats);
        return ResponseEntity.ok(stats);
    }

    // --- SANCIONES ---

    @GetMapping("/sanciones")
    public ResponseEntity<List<SancionEntidad>> listarSanciones() {
        System.out.println("[API ADMIN] GET /sanciones called");
        List<SancionEntidad> sanciones = sancionRepositorio.findAll();
        System.out.println(
                "[API ADMIN] GET /sanciones returning " + (sanciones != null ? sanciones.size() : 0) + " items");
        return ResponseEntity.ok(sanciones);
    }

    @PostMapping("/sanciones/{id}/resolver")
    public ResponseEntity<?> resolverSancion(@PathVariable Integer id,
            @RequestParam String resolucion,
            @RequestParam String accion,
            @RequestParam Integer adminId) {

        if (id == null || adminId == null) {
            return ResponseEntity.badRequest().body("ID y AdminID son obligatorios");
        }

        return sancionRepositorio.findById(id).map(sancion -> {
            UsuarioEntidad admin = usuarioRepositorio.findById(adminId).orElse(null);
            if (admin == null) {
                return ResponseEntity.badRequest().body("Admin no encontrado");
            }

            sancion.setAdmin(admin);
            sancion.setResolucion(resolucion);
            sancion.setEstado("RESUELTO");

            // LOG
            logger.logAction(admin.getEmail(), "RESOLVE_SANCTION",
                    "Resolved sanction " + id + " with action: " + accion);

            UsuarioEntidad usuario = sancion.getUsuario();
            if (usuario != null) {
                switch (accion) {
                    case "QUITAR":
                        usuario.setVetado(false);
                        usuario.setVetadoHasta(null);
                        sancion.setFechaFin(LocalDateTime.now());
                        break;
                    case "PERMANENTE":
                        usuario.setVetado(true);
                        usuario.setVetadoHasta(LocalDateTime.now().plusYears(100));
                        sancion.setTipo("PERMANENTE");
                        break;
                    case "REDUCIR":
                        if (usuario.getVetadoHasta() != null) {
                            usuario.setVetadoHasta(LocalDateTime.now().plusDays(1));
                        }
                        break;
                    case "MANTENER":
                    default:
                        break;
                }
                usuarioRepositorio.save(usuario);
            }

            sancionRepositorio.save(sancion);
            return ResponseEntity.ok(sancion);
        }).orElse(ResponseEntity.notFound().build());
    }

    // --- USUARIOS VETADOS ---

    @GetMapping("/vetados")
    public ResponseEntity<List<UsuarioEntidad>> listarVetados() {
        System.out.println("[API ADMIN] GET /vetados called");
        List<UsuarioEntidad> vetados = usuarioRepositorio.findByVetadoTrue();
        System.out.println("[API ADMIN] GET /vetados returning " + (vetados != null ? vetados.size() : 0) + " items");
        return ResponseEntity.ok(vetados);
    }

    // --- DENUNCIAS ---

    @GetMapping("/denuncias")
    public ResponseEntity<List<DenunciaEntidad>> listarDenuncias() {
        System.out.println("[API ADMIN] GET /denuncias called");
        List<DenunciaEntidad> denuncias = denunciaRepositorio.findAll();
        System.out.println(
                "[API ADMIN] GET /denuncias returning " + (denuncias != null ? denuncias.size() : 0) + " items");
        return ResponseEntity.ok(denuncias);
    }

    @PostMapping("/denuncias/{id}/resolver")
    public ResponseEntity<?> resolverDenuncia(@PathVariable Integer id, @RequestParam String estado) {
        if (id == null)
            return ResponseEntity.badRequest().build();

        return denunciaRepositorio.findById(id).map(denuncia -> {
            denuncia.setEstado(estado);
            denunciaRepositorio.save(denuncia);
            return ResponseEntity.ok(denuncia);
        }).orElse(ResponseEntity.notFound().build());
    }
}
