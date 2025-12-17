package com.noticias.api.config;

import com.noticias.api.entidades.UsuarioEntidad;
import com.noticias.api.entidades.CategoriaEntidad;
import com.noticias.api.entidades.NoticiaEntidad;
import com.noticias.api.repositorios.UsuarioRepositorio;
import com.noticias.api.repositorios.CategoriaRepositorio;
import com.noticias.api.repositorios.NoticiaRepositorio;
import com.noticias.api.repositorios.RolRepositorio;
import com.noticias.api.entidades.RolEntidad;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private NoticiaRepositorio noticiaRepositorio;

    @Autowired
    private UsuarioRepositorio usuarioRepositorio;

    @Autowired
    private CategoriaRepositorio categoriaRepositorio;

    @Autowired
    private RolRepositorio rolRepositorio;

    @Override
    public void run(String... args) throws Exception {
        try {
            forceOwnerRole();
            if (noticiaRepositorio.count() == 0) {
                seedFirstNews();
            }
        } catch (Exception e) {
            // Log full stack trace properly
            e.printStackTrace();
        }
    }

    private void forceOwnerRole() {
        String email = "antoniowebserver@gmail.com";
        UsuarioEntidad usuario = usuarioRepositorio.findByEmail(email).orElse(null);
        if (usuario != null) {
            RolEntidad rolOwner = rolRepositorio.findByNombre("Owner").orElse(null);
            if (rolOwner != null) {
                if (usuario.getRol() == null || !"Owner".equals(usuario.getRol().getNombre())) {
                    usuario.setRol(rolOwner);
                    // Legacy support just in case
                    usuario.setEsSuperAdmin(true);
                    usuario.setVetado(false);
                    usuarioRepositorio.save(usuario);
                    System.out.println("INFO: FORCED Owner role for user: " + email);
                } else {
                    System.out.println("INFO: User " + email + " already has Owner role.");
                }
            } else {
                System.err.println("ERROR: Critical: Owner Role not found in DB.");
            }
        } else {
            System.out.println("WARN: User " + email + " not found. Cannot assign Owner role.");
        }
    }

    private void seedFirstNews() {
        UsuarioEntidad autor = usuarioRepositorio.findAll().stream().findFirst().orElse(null);
        if (autor == null) {
            System.out.println("⚠️ No user found to be author of first news.");
            return;
        }

        CategoriaEntidad categoria = categoriaRepositorio.findAll().stream().findFirst().orElse(null);
        if (categoria == null) {
            System.out.println("⚠️ No category found for first news.");
            return;
        }

        NoticiaEntidad noticia = new NoticiaEntidad();
        noticia.setTitulo("¡Bienvenidos a NoticiasApp!");
        noticia.setSubtitulo("El comienzo de una nueva era informativa.");
        noticia.setContenido(
                "Estamos emocionados de lanzar nuestra nueva plataforma. Aquí encontrarás las últimas noticias, opiniones y debates de nuestra comunidad. ¡Únete a nosotros!");
        noticia.setImagenUrl(
                "https://images.unsplash.com/photo-1504711434969-e33886168f5c?auto=format&fit=crop&q=80&w=1000"); // Generic
                                                                                                                  // news
                                                                                                                  // image
        noticia.setFechaPublicacion(LocalDateTime.now());
        noticia.setAutor(autor);
        noticia.setCategoria(categoria);
        noticia.setVisitas(0);
        noticia.setLikes(0);
        noticia.setDislikes(0);

        noticiaRepositorio.save(noticia);
        System.out.println("✅ First news item seeded successfully!");
    }
}
