package com.noticias.api.controladores;

import com.noticias.api.entidades.CategoriaEntidad;
import com.noticias.api.servicios.CategoriaServicio;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author amorcia
 *         CLASE - Controlador REST para la gestión completa de categorías de
 *         noticias.
 *         Proporciona endpoints para CRUD de categorías, incluyendo manejo de
 *         jerarquías
 *         (categorías raíz y subcategorías).
 */
@RestController
@RequestMapping("/categorias")
@CrossOrigin(origins = "*")
public class CategoriaControlador {

    private final CategoriaServicio categoriaServicio;

    /**
     * @author amorcia
     *         METODO - Constructor del controlador con inyección de dependencias
     * @param categoriaServicio Servicio de lógica de negocio de categorías
     */
    public CategoriaControlador(CategoriaServicio categoriaServicio) {
        this.categoriaServicio = categoriaServicio;
    }

    /**
     * @author amorcia
     *         METODO - Obtiene la lista completa de todas las categorías
     * @return ResponseEntity con lista de categorías
     */
    @GetMapping
    public ResponseEntity<List<CategoriaEntidad>> listarTodas() {
        return ResponseEntity.ok(categoriaServicio.listarTodas());
    }

    /**
     * @author amorcia
     *         METODO - Obtiene solo las categorías raíz (sin padre)
     * @return ResponseEntity con lista de categorías raíz
     */
    @GetMapping("/raiz")
    public ResponseEntity<List<CategoriaEntidad>> listarCategoriasRaiz() {
        return ResponseEntity.ok(categoriaServicio.listarCategoriasRaiz());
    }

    /**
     * @author amorcia
     *         METODO - Obtiene las subcategorías de una categoría específica
     * @param id ID de la categoría padre
     * @return ResponseEntity con lista de subcategorías
     */
    @GetMapping("/{id}/subcategorias")
    public ResponseEntity<List<CategoriaEntidad>> listarSubcategorias(@PathVariable Integer id) {
        return ResponseEntity.ok(categoriaServicio.listarSubcategorias(id));
    }

    /**
     * @author amorcia
     *         METODO - Busca una categoría específica por su ID
     * @param id ID de la categoría a buscar
     * @return ResponseEntity con la categoría encontrada o 404 si no existe
     */
    @GetMapping("/{id}")
    public ResponseEntity<CategoriaEntidad> buscarPorId(@PathVariable Integer id) {
        return categoriaServicio.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * @author amorcia
     *         METODO - Busca una categoría por su nombre
     * @param nombre Nombre de la categoría a buscar
     * @return ResponseEntity con la categoría encontrada o 404 si no existe
     */
    @GetMapping("/nombre/{nombre}")
    public ResponseEntity<CategoriaEntidad> buscarPorNombre(@PathVariable String nombre) {
        return categoriaServicio.buscarPorNombre(nombre)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * @author amorcia
     *         METODO - Crea una nueva categoría en el sistema
     * @param categoria Entidad de la categoría a crear
     * @return ResponseEntity con la categoría creada (201)
     */
    @PostMapping
    public ResponseEntity<CategoriaEntidad> crear(@RequestBody CategoriaEntidad categoria) {
        CategoriaEntidad creada = categoriaServicio.crearCategoria(categoria);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    /**
     * @author amorcia
     *         METODO - Actualiza los datos de una categoría existente
     * @param id        ID de la categoría a actualizar
     * @param categoria Datos actualizados de la categoría
     * @return ResponseEntity con categoría actualizada o 404 si no existe
     */
    @PutMapping("/{id}")
    public ResponseEntity<CategoriaEntidad> actualizar(@PathVariable Integer id,
            @RequestBody CategoriaEntidad categoria) {
        CategoriaEntidad actualizada = categoriaServicio.actualizarCategoria(id, categoria);
        if (actualizada != null) {
            return ResponseEntity.ok(actualizada);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * @author amorcia
     *         METODO - Elimina una categoría del sistema
     * @param id ID de la categoría a eliminar
     * @return ResponseEntity vacío con 204 si éxito, 404 si no existe
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        if (categoriaServicio.eliminarCategoria(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
