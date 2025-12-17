package com.noticias.api.repositorios;

import com.noticias.api.entidades.RolEntidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RolRepositorio extends JpaRepository<RolEntidad, Integer> {
    Optional<RolEntidad> findByNombre(String nombre);
}
