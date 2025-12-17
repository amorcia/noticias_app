package com.noticias.api.controladores;

import com.noticias.api.entidades.CategoriaEntidad;
import com.noticias.api.servicios.CategoriaServicio;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gestión de categorías.
 */
@RestController
@RequestMapping("/categorias")
@CrossOrigin(origins = "*")
public class CategoriaControlador {

    private final CategoriaServicio categoriaServicio;

    public CategoriaControlador(CategoriaServicio categoriaServicio) {
        this.categoriaServicio = categoriaServicio;
    }

    @GetMapping
    public ResponseEntity<List<CategoriaEntidad>> listarTodas() {
        return ResponseEntity.ok(categoriaServicio.listarTodas());
    }

    @GetMapping("/raiz")
    public ResponseEntity<List<CategoriaEntidad>> listarCategoriasRaiz() {
        return ResponseEntity.ok(categoriaServicio.listarCategoriasRaiz());
    }

    @GetMapping("/{id}/subcategorias")
    public ResponseEntity<List<CategoriaEntidad>> listarSubcategorias(@PathVariable Integer id) {
        return ResponseEntity.ok(categoriaServicio.listarSubcategorias(id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoriaEntidad> buscarPorId(@PathVariable Integer id) {
        return categoriaServicio.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/nombre/{nombre}")
    public ResponseEntity<CategoriaEntidad> buscarPorNombre(@PathVariable String nombre) {
        return categoriaServicio.buscarPorNombre(nombre)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<CategoriaEntidad> crear(@RequestBody CategoriaEntidad categoria) {
        CategoriaEntidad creada = categoriaServicio.crearCategoria(categoria);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoriaEntidad> actualizar(@PathVariable Integer id,
            @RequestBody CategoriaEntidad categoria) {
        CategoriaEntidad actualizada = categoriaServicio.actualizarCategoria(id, categoria);
        if (actualizada != null) {
            return ResponseEntity.ok(actualizada);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        if (categoriaServicio.eliminarCategoria(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
