package com.noticias.web.controladores;

import com.noticias.web.dtos.CategoriaDTO;
import com.noticias.web.servicios.ApiNoticiasCliente;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

@ControllerAdvice
public class GlobalControllerAdvice {

    private final ApiNoticiasCliente apiCliente;

    public GlobalControllerAdvice(ApiNoticiasCliente apiCliente) {
        this.apiCliente = apiCliente;
    }

    @ModelAttribute("categorias")
    public List<CategoriaDTO> cargarCategoriasGlobal() {
        try {
            return apiCliente.listarCategoriasRaiz();
        } catch (Exception e) {
            return List.of();
        }
    }
}
