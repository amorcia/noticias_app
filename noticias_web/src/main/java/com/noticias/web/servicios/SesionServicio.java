package com.noticias.web.servicios;

import com.noticias.web.dtos.UsuarioDTO;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

@Service
public class SesionServicio {

    public boolean validarSesion(HttpSession session) {
        return session.getAttribute("usuario") != null;
    }

    public UsuarioDTO obtenerUsuarioLogueado(HttpSession session) {
        return (UsuarioDTO) session.getAttribute("usuario");
    }
}
