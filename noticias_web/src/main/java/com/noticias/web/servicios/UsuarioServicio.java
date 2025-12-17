package com.noticias.web.servicios;

import com.noticias.web.dtos.UsuarioDTO;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Servicio para operaciones relacionadas con usuarios.
 */
@Service
public class UsuarioServicio {

    private final ApiNoticiasCliente apiCliente;

    public UsuarioServicio(ApiNoticiasCliente apiCliente) {
        this.apiCliente = apiCliente;
    }

    public List<UsuarioDTO> listarTodos() {
        return apiCliente.listarUsuarios();
    }

    public UsuarioDTO buscarPorId(Integer id) {
        return apiCliente.buscarUsuarioPorId(id);
    }

    public UsuarioDTO buscarPorEmail(String email) {
        return apiCliente.buscarUsuarioPorEmail(email);
    }

    public boolean vetarUsuario(Integer id, String motivo) {
        return apiCliente.vetarUsuario(id, motivo);
    }

    public boolean desvetarUsuario(Integer id) {
        return apiCliente.desvetarUsuario(id);
    }
}
