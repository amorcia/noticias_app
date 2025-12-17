package com.noticias.api.repositorios;

import com.noticias.api.entidades.SancionEntidad;
import com.noticias.api.entidades.UsuarioEntidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SancionRepositorio extends JpaRepository<SancionEntidad, Integer> {
    List<SancionEntidad> findByUsuario(UsuarioEntidad usuario);

    List<SancionEntidad> findByEstado(String estado);
}
