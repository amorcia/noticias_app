package com.noticias.api.repositorios;

import com.noticias.api.entidades.ErrorLogEntidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ErrorLogRepositorio extends JpaRepository<ErrorLogEntidad, Long> {

    List<ErrorLogEntidad> findByUsuarioIdOrderByTimestampDesc(Integer usuarioId);

    List<ErrorLogEntidad> findByTipoErrorOrderByTimestampDesc(String tipoError);

    List<ErrorLogEntidad> findTop50ByOrderByTimestampDesc();

    @Modifying
    @Query("DELETE FROM ErrorLogEntidad e WHERE e.ultimaActividad < :fechaLimite")
    void eliminarLogsAntiguos(LocalDateTime fechaLimite);
}
