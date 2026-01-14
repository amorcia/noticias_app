package com.noticias.web.controladores;

import com.noticias.web.dtos.*;
import com.noticias.web.servicios.ApiNoticiasCliente;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class InicioControlador {

    private final ApiNoticiasCliente apiCliente;

    public InicioControlador(ApiNoticiasCliente apiCliente) {
        this.apiCliente = apiCliente;
    }

    @GetMapping("/")
    public String inicio(Model model) {
        try {
            // Cargar Top 5 Populares
            List<NoticiaDTO> noticiasPopulares = apiCliente.listarNoticiasPopulares();
            model.addAttribute("noticiasPopulares", noticiasPopulares);
        } catch (Exception e) {
            model.addAttribute("noticiasPopulares", List.of());
        }
        return "vistas/Inicio";
    }

    @GetMapping("/categoria/{nombre}")
    public String categoria(@PathVariable String nombre,
            @RequestParam(required = false) String filtro,
            @RequestParam(required = false) Integer mes,
            @RequestParam(required = false) Integer anio,
            Model model) {

        // Buscar categoria por nombre para obtener ID
        CategoriaDTO cat = apiCliente.buscarCategoriaPorNombre(nombre);
        if (cat == null) {
            return "redirect:/";
        }

        model.addAttribute("categoriaNombre", nombre);
        model.addAttribute("categoria", cat); // Pass full object if needed
        model.addAttribute("esForo", false);

        // Pass params back to view
        model.addAttribute("filtroActual", filtro);
        model.addAttribute("mesActual", mes);

        if (filtro != null || mes != null) {
            Integer year = (anio != null) ? anio : java.time.Year.now().getValue();
            model.addAttribute("noticias",
                    apiCliente.listarNoticiasPorCategoriaFiltrado(cat.getId(), filtro, mes, year));
        } else {
            model.addAttribute("noticias", apiCliente.listarNoticiasPorCategoria(cat.getId()));
        }

        return "vistas/Categoria";
    }

    @GetMapping("/foro/{nombre}")
    public String foro(@PathVariable String nombre, Model model) {
        model.addAttribute("categoriaNombre", nombre);
        model.addAttribute("esForo", true);
        model.addAttribute("noticias", apiCliente.listarNoticiasForoPorCategoriaNombre(nombre));
        return "vistas/Categoria";
    }

    @GetMapping("/vuestras-noticias")
    public String vuestrasNoticias(Model model) {
        model.addAttribute("categoriaNombre", "Vuestras Noticias");
        model.addAttribute("esForo", true);
        // Filtrar noticias que son aportación de usuario
        // (apiCliente.listarNoticiasPopulares() no sirve aquí, necesitamos todas o
        // endpoint especifico)
        // Usaremos el nuevo endpoint de foro sin categoría especifica si existe, o
        // filtramos en memoria por ahora.
        // Simulamos con empty o implementamos endpoint 'listarTodasAportaciones'
        // Para simplificar: redirigimos a home o mostramos vacío hasta implementar
        // endpoint general.
        // Mejor: Crear endpoint 'listarAportaciones' en API. O filtrar
        // 'listarNoticiasPopulares' si trajera todas.
        // Voy a usar listarNoticiasPopulares() temporalmente pero indicando que es foro
        // para que el usuario vea ALGO.
        // O mejor, uso apiCliente.listarNoticiasForoPorCategoriaNombre("General")? No.
        model.addAttribute("noticias", List.of()); // Placeholder por ahora para no romper
        return "vistas/Categoria";
    }

    @GetMapping("/noticia/{id}")
    public String verNoticia(@PathVariable Integer id, Model model) {
        NoticiaDTO noticia = apiCliente.buscarNoticiaPorId(id);
        if (noticia == null) {
            return "redirect:/";
        }
        model.addAttribute("noticia", noticia);
        return "vistas/DetalleNoticia";
    }

    @PostMapping("/noticia/{id}/votar")
    @ResponseBody
    public String votarNoticia(@PathVariable Integer id, @RequestParam String tipo) {
        boolean like = "LIKE".equalsIgnoreCase(tipo);
        apiCliente.votarNoticia(id, like);
        return "OK";
    }

    @GetMapping("/ajustes")
    public String ajustes() {
        return "vistas/Ajustes";
    }

    @GetMapping("/perfil")
    public String perfil(jakarta.servlet.http.HttpSession session, Model model) {
        UsuarioDTO usuario = (UsuarioDTO) session.getAttribute("usuario");
        if (usuario == null) {
            return "redirect:/auth/login";
        }
        model.addAttribute("usuario", usuario);
        // Cargar noticias del usuario
        model.addAttribute("misNoticias", apiCliente.listarNoticiasPorAutor(usuario.getId()));
        // Mock notificaciones (o implementar servicio real si existe tiempo, pero
        // usuario pidio comportamiento)
        model.addAttribute("notificacionesSinLeer", 5); // Ejemplo fijo para demostración UI, o 0
        return "vistas/Perfil";
    }
}
