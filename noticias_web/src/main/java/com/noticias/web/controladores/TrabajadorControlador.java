package com.noticias.web.controladores;

import com.noticias.web.dtos.NoticiaDTO;
import com.noticias.web.servicios.ApiNoticiasCliente;
import com.noticias.web.servicios.SesionServicio;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/trabajador")
public class TrabajadorControlador {

    @Autowired
    private SesionServicio sesionServicio;

    @Autowired
    private ApiNoticiasCliente apiCliente;

    @GetMapping("/subir-noticia")
    public String formularioSubirNoticia(HttpSession session, Model model) {
        if (!sesionServicio.validarSesion(session)) {
            return "redirect:/auth/login";
        }

        // Verificar rol TRABAJADOR, ADMIN u OWNER (Case Insensitive)
        String rol = sesionServicio.obtenerUsuarioLogueado(session).getRol(); // getRol returns rolNombre
        boolean accesoPermitido = "TRABAJADOR".equalsIgnoreCase(rol) || "ADMIN".equalsIgnoreCase(rol)
                || "OWNER".equalsIgnoreCase(rol);

        if (!accesoPermitido) {
            return "redirect:/";
        }

        // model.addAttribute("categorias", ...); // Suministrado por GlobalAdvice
        model.addAttribute("categorias", apiCliente.listarCategorias());
        model.addAttribute("noticia", new NoticiaDTO());

        return "vistas/FormularioNoticia";
    }

    @org.springframework.web.bind.annotation.PostMapping("/publicar")
    public String publicarNoticia(
            @org.springframework.web.bind.annotation.RequestParam("titulo") String titulo,
            @org.springframework.web.bind.annotation.RequestParam("subtitulo") String subtitulo,
            @org.springframework.web.bind.annotation.RequestParam("contenido") String contenido,
            @org.springframework.web.bind.annotation.RequestParam("categoriaId") Integer categoriaId,
            @org.springframework.web.bind.annotation.RequestParam(value = "file", required = false) org.springframework.web.multipart.MultipartFile file,
            HttpSession session,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {

        if (!sesionServicio.validarSesion(session)) {
            return "redirect:/auth/login";
        }

        Integer usuarioId = sesionServicio.obtenerUsuarioLogueado(session).getId();

        String error = apiCliente.publicarNoticia(titulo, subtitulo, contenido, categoriaId, usuarioId, file);

        if (error != null) {
            // Error (posiblemente NSFW o Veto)
            System.out.println("❌ Error publicando noticia: " + error);
            redirectAttributes.addFlashAttribute("error", error);
            // Si el error es veto, tal vez deberíamos cerrar sesión forzosamente
            if (error.contains("vetado")) {
                session.invalidate();
                return "redirect:/auth/login?error="
                        + java.net.URLEncoder.encode(error, java.nio.charset.StandardCharsets.UTF_8);
            }
            return "redirect:/trabajador/subir-noticia";
        }

        redirectAttributes.addFlashAttribute("mensaje", "Noticia publicada correctamente");
        return "redirect:/";
    }

    @org.springframework.web.bind.annotation.PostMapping("/noticias/borrar")
    public org.springframework.http.ResponseEntity<?> borrarNoticia(
            @org.springframework.web.bind.annotation.RequestParam(required = false) Integer id,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String titulo,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String motivo,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String descripcion,
            @org.springframework.web.bind.annotation.RequestParam(required = false) Integer eliminadorId,
            @org.springframework.web.bind.annotation.RequestHeader(value = "X-Requested-With", required = false) String requestedWith,
            HttpSession session,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {

        if (!sesionServicio.validarSesion(session)) {
            return org.springframework.http.ResponseEntity.status(401).body("Sesión no válida");
        }

        var usuario = sesionServicio.obtenerUsuarioLogueado(session);
        NoticiaDTO noticia = null;

        if (id != null) {
            noticia = apiCliente.buscarNoticiaPorId(id);
        } else if (titulo != null && !titulo.isBlank()) {
            noticia = apiCliente.buscarNoticiaPorTitulo(titulo);
        }

        if (noticia == null) {
            return org.springframework.http.ResponseEntity.status(404).body("Noticia no encontrada");
        }

        // Permisos: Dueño de la noticia, Admin o Owner del sitio
        boolean esAutor = noticia.getAutorId() != null && noticia.getAutorId().equals(usuario.getId());
        String rol = usuario.getRol();
        boolean esAdmin = "ADMIN".equalsIgnoreCase(rol) || "OWNER".equalsIgnoreCase(rol);

        if (!esAutor && !esAdmin) {
            return org.springframework.http.ResponseEntity.status(403)
                    .body("No tienes permiso para borrar esta noticia");
        }

        // Si es el autor eliminando su propia noticia, pasar null para no archivar
        // Si es admin eliminando noticia de otro, validar y pasar motivo/descripción
        String motivoFinal = null;
        String descripcionFinal = null;

        if (!esAutor && esAdmin) {
            // Es admin eliminando noticia de otro - requiere motivo y descripción
            if (motivo == null || motivo.isBlank()) {
                return org.springframework.http.ResponseEntity.status(400)
                        .body("El motivo es obligatorio para eliminaciones administrativas");
            }
            if (descripcion == null || descripcion.isBlank()) {
                return org.springframework.http.ResponseEntity.status(400)
                        .body("La descripción es obligatoria para eliminaciones administrativas");
            }
            motivoFinal = motivo;
            descripcionFinal = descripcion;
        }
        // Si esAutor, motivoFinal y descripcionFinal quedan como null

        boolean exito = false;
        if (id != null) {
            exito = apiCliente.eliminarNoticia(id, motivoFinal, usuario.getId());
        } else {
            exito = apiCliente.eliminarNoticiaPorTitulo(titulo, motivoFinal, descripcionFinal, usuario.getId());
        }

        if (exito) {
            return org.springframework.http.ResponseEntity.ok("Noticia eliminada");
        } else {
            return org.springframework.http.ResponseEntity.status(500).body("Error al eliminar la noticia");
        }
    }

    @GetMapping("/noticias/editar/{id}")
    public String formularioEditarNoticia(@PathVariable Integer id, HttpSession session, Model model) {
        if (!sesionServicio.validarSesion(session)) {
            return "redirect:/auth/login";
        }

        NoticiaDTO noticia = apiCliente.buscarNoticiaPorId(id);
        if (noticia == null) {
            return "redirect:/";
        }

        var usuario = sesionServicio.obtenerUsuarioLogueado(session);
        boolean esAutor = noticia.getAutorId() != null && noticia.getAutorId().equals(usuario.getId());
        String rol = usuario.getRol();
        boolean esAdmin = "ADMIN".equalsIgnoreCase(rol) || "OWNER".equalsIgnoreCase(rol);

        if (!esAutor && !esAdmin) {
            return "redirect:/";
        }

        model.addAttribute("categorias", apiCliente.listarCategorias());
        model.addAttribute("noticia", noticia);
        return "vistas/FormularioEdicion";
    }

    @org.springframework.web.bind.annotation.PostMapping("/noticias/editar")
    public String editarNoticia(
            @org.springframework.web.bind.annotation.RequestParam("id") Integer id,
            @org.springframework.web.bind.annotation.RequestParam("titulo") String titulo,
            @org.springframework.web.bind.annotation.RequestParam("subtitulo") String subtitulo,
            @org.springframework.web.bind.annotation.RequestParam("contenido") String contenido,
            @org.springframework.web.bind.annotation.RequestParam("categoriaId") Integer categoriaId,
            @org.springframework.web.bind.annotation.RequestParam(value = "file", required = false) org.springframework.web.multipart.MultipartFile file,
            HttpSession session,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {

        if (!sesionServicio.validarSesion(session)) {
            return "redirect:/auth/login";
        }

        String error = apiCliente.editarNoticia(id, titulo, subtitulo, contenido, categoriaId, file);

        if (error != null) {
            redirectAttributes.addFlashAttribute("error", error);
            return "redirect:/trabajador/noticias/editar/" + id;
        }

        redirectAttributes.addFlashAttribute("mensaje", "Noticia actualizada correctamente");
        return "redirect:/noticia/" + id;
    }
}
