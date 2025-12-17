package com.noticias.api.repositorios;

import com.noticias.api.entidades.EtiquetaEntidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EtiquetaRepositorio extends JpaRepository<EtiquetaEntidad, Integer> {
    Optional<EtiquetaEntidad> findByNombre(String nombre);
}
