package com.noticias.web.controladores;

import com.noticias.web.dtos.NoticiaDTO;
import com.noticias.web.servicios.ApiNoticiasCliente;
import com.noticias.web.servicios.SesionServicio;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.http.ResponseEntity;

@Controller
@RequestMapping("/trabajador")
public class TrabajadorControlador {

    @Autowired
    private SesionServicio sesionServicio;

    @Autowired
    private ApiNoticiasCliente apiCliente;

    @Autowired
    private com.noticias.web.servicios.LoggerService logger;

    /**
     * @author amorcia
     *         METODO - Muestra el formulario para subir una nueva noticia
     */
    @GetMapping("/subir-noticia")
    public String formularioSubirNoticia(HttpSession session, Model model) {
        if (!sesionServicio.validarSesion(session))
            return "redirect:/auth/login";

        String rol = sesionServicio.obtenerUsuarioLogueado(session).getRol();
        boolean accesoPermitido = "TRABAJADOR".equalsIgnoreCase(rol) || "ADMIN".equalsIgnoreCase(rol)
                || "OWNER".equalsIgnoreCase(rol);

        if (!accesoPermitido)
            return "redirect:/";

        model.addAttribute("categorias", apiCliente.listarCategorias());
        model.addAttribute("noticia", new NoticiaDTO());

        return "vistas/FormularioNoticia";
    }

    /**
     * @author amorcia
     *         METODO - Procesa la publicación de una noticia
     */
    @PostMapping("/publicar")
    public String publicarNoticia(
            @RequestParam("titulo") String titulo,
            @RequestParam("subtitulo") String subtitulo,
            @RequestParam("contenido") String contenido,
            @RequestParam("categoriaId") Integer categoriaId,
            @RequestParam(value = "file", required = false) MultipartFile file,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (!sesionServicio.validarSesion(session))
            return "redirect:/auth/login";

        Integer usuarioId = sesionServicio.obtenerUsuarioLogueado(session).getId();

        String error = apiCliente.publicarNoticia(titulo, subtitulo, contenido, categoriaId, usuarioId, file);

        if (error != null) {
            System.out.println("❌ Error publicando noticia: " + error);
            redirectAttributes.addFlashAttribute("error", error);
            if (error.contains("vetado")) {
                session.invalidate();
                return "redirect:/auth/login?error="
                        + java.net.URLEncoder.encode(error, java.nio.charset.StandardCharsets.UTF_8);
            }
            return "redirect:/trabajador/subir-noticia";
        }

        // Log publication success
        logger.logAction(sesionServicio.obtenerUsuarioLogueado(session).getEmail(), "PUBLISH_NEWS",
                "Has publicado una noticia: " + titulo);

        redirectAttributes.addFlashAttribute("mensaje", "Noticia publicada correctamente");
        return "redirect:/";
    }

    /**
     * @author amorcia
     *         METODO - Borra una noticia (Autor o Admin/Owner)
     */
    @PostMapping("/noticias/borrar")
    public ResponseEntity<?> borrarNoticia(
            @RequestParam(required = false) Integer id,
            @RequestParam(required = false) String titulo,
            @RequestParam(required = false) String motivo,
            @RequestParam(required = false) String descripcion,
            @RequestParam(required = false) Integer eliminadorId,
            @RequestHeader(value = "X-Requested-With", required = false) String requestedWith,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (!sesionServicio.validarSesion(session))
            return ResponseEntity.status(401).body("Sesión no válida");

        var usuario = sesionServicio.obtenerUsuarioLogueado(session);
        NoticiaDTO noticia = null;

        if (id != null) {
            noticia = apiCliente.buscarNoticiaPorId(id);
        } else if (titulo != null && !titulo.isBlank()) {
            noticia = apiCliente.buscarNoticiaPorTitulo(titulo);
        }

        if (noticia == null)
            return ResponseEntity.status(404).body("Noticia no encontrada");

        boolean esAutor = noticia.getAutorId() != null && noticia.getAutorId().equals(usuario.getId());
        String rol = usuario.getRol();
        boolean esAdmin = "ADMIN".equalsIgnoreCase(rol) || "OWNER".equalsIgnoreCase(rol);

        if (!esAutor && !esAdmin)
            return ResponseEntity.status(403).body("No tienes permiso para borrar esta noticia");

        boolean exito = false;

        if (esAutor) {
            exito = apiCliente.eliminarNoticiaConConfirmacion(noticia.getId(), titulo, usuario.getId());
        } else {
            if (motivo == null || motivo.isBlank())
                return ResponseEntity.status(400).body("El motivo es obligatorio para eliminaciones administrativas");
            if (descripcion == null || descripcion.isBlank())
                return ResponseEntity.status(400)
                        .body("La descripción es obligatoria para eliminaciones administrativas");
            exito = apiCliente.eliminarNoticiaConJustificacion(noticia.getId(), motivo, descripcion, usuario.getId());
        }

        if (exito) {
            return ResponseEntity.ok("Noticia eliminada");
        } else {
            return ResponseEntity.status(500).body("Error al eliminar la noticia. Verifica el título o los permisos.");
        }
    }

    /**
     * @author amorcia
     *         METODO - Formulario para editar noticia
     */
    @GetMapping("/noticias/editar/{id}")
    public String formularioEditarNoticia(@PathVariable Integer id, HttpSession session, Model model) {
        if (!sesionServicio.validarSesion(session))
            return "redirect:/auth/login";

        NoticiaDTO noticia = apiCliente.buscarNoticiaPorId(id);
        if (noticia == null)
            return "redirect:/";

        var usuario = sesionServicio.obtenerUsuarioLogueado(session);
        boolean esAutor = noticia.getAutorId() != null && noticia.getAutorId().equals(usuario.getId());
        String rol = usuario.getRol();
        boolean esAdmin = "ADMIN".equalsIgnoreCase(rol) || "OWNER".equalsIgnoreCase(rol);

        if (!esAutor && !esAdmin)
            return "redirect:/";

        model.addAttribute("categorias", apiCliente.listarCategorias());
        model.addAttribute("noticia", noticia);
        return "vistas/FormularioEdicion";
    }

    /**
     * @author amorcia
     *         METODO - Procesa edición de noticia
     */
    @PostMapping("/noticias/editar")
    public String editarNoticia(
            @RequestParam("id") Integer id,
            @RequestParam("titulo") String titulo,
            @RequestParam("subtitulo") String subtitulo,
            @RequestParam("contenido") String contenido,
            @RequestParam("categoriaId") Integer categoriaId,
            @RequestParam(value = "file", required = false) MultipartFile file,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (!sesionServicio.validarSesion(session))
            return "redirect:/auth/login";

        String error = apiCliente.editarNoticia(id, titulo, subtitulo, contenido, categoriaId, file);

        if (error != null) {
            redirectAttributes.addFlashAttribute("error", error);
            return "redirect:/trabajador/noticias/editar/" + id;
        }

        redirectAttributes.addFlashAttribute("mensaje", "Noticia actualizada correctamente");
        return "redirect:/noticia/" + id;
    }
}
