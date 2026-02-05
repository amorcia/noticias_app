package com.noticias.api.repositorios;

import com.noticias.api.entidades.UsuarioEliminadoEntidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UsuarioEliminadoRepositorio extends JpaRepository<UsuarioEliminadoEntidad, Long> {

    List<UsuarioEliminadoEntidad> findByEliminadoPorId(Integer eliminadorId);

    List<UsuarioEliminadoEntidad> findByFechaEliminacionBetween(LocalDateTime inicio, LocalDateTime fin);
}
