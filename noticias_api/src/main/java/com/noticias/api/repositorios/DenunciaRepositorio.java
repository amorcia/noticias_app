package com.noticias.api.repositorios;

import com.noticias.api.entidades.DenunciaEntidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DenunciaRepositorio extends JpaRepository<DenunciaEntidad, Integer> {
    List<DenunciaEntidad> findByEstado(String estado);

    List<DenunciaEntidad> findByTipoAndIdObjeto(String tipo, Integer idObjeto);
}
