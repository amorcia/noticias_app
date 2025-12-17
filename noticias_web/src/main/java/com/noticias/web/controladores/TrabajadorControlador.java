package com.noticias.web.controladores;

import com.noticias.web.dtos.NoticiaDTO;
import com.noticias.web.servicios.ApiNoticiasCliente;
import com.noticias.web.servicios.SesionServicio;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
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

        // Cargar categorías para el dropdown
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
            @org.springframework.web.bind.annotation.RequestParam("file") org.springframework.web.multipart.MultipartFile file,
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
}
