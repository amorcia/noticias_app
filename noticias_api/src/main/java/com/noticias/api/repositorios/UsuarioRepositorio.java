package com.noticias.api.repositorios;

import com.noticias.api.entidades.UsuarioEntidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepositorio extends JpaRepository<UsuarioEntidad, Integer> {
    Optional<UsuarioEntidad> findByEmail(String email);

    Optional<UsuarioEntidad> findByCodigoVerificacion(String codigo);

    // findByCodigoRecuperacion eliminado

    Optional<UsuarioEntidad> findByTokenSession(String token);

    java.util.List<UsuarioEntidad> findByVetadoTrue();
}
