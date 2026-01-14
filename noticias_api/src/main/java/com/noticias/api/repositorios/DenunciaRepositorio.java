package com.noticias.api.repositorios;

import com.noticias.api.entidades.DenunciaEntidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DenunciaRepositorio extends JpaRepository<DenunciaEntidad, Integer> {
}
