package com.noticias.web.seguridad;

import com.noticias.web.dtos.UsuarioDTO;
import com.noticias.web.servicios.ApiNoticiasCliente;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final ApiNoticiasCliente apiCliente;

    public CustomUserDetailsService(ApiNoticiasCliente apiCliente) {
        this.apiCliente = apiCliente;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UsuarioDTO usuarioDTO = apiCliente.buscarUsuarioPorEmail(email);

        if (usuarioDTO == null) {
            throw new UsernameNotFoundException("Usuario no encontrado con email: " + email);
        }

        String rol = usuarioDTO.getRolNombre();
        if (rol == null) {
            rol = "USER";
        }
        if (!rol.startsWith("ROLE_")) {
            rol = "ROLE_" + rol.toUpperCase();
        }

        List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority(rol));

        return new User(
                usuarioDTO.getEmail(),
                usuarioDTO.getPassword(),
                Boolean.TRUE.equals(usuarioDTO.getActivo()),
                true,
                true,
                true,
                authorities);
    }
}
