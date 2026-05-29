package com.lab.productos.controller;

import com.lab.productos.dto.CrearProductoRequest;
import com.lab.productos.exception.ResourceNotFoundException;
import com.lab.productos.model.Producto;
import com.lab.productos.service.ProductoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/productos")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping
    public List<Producto> listar() {
        return productoService.listar();
    }

    @GetMapping("/{id}")
    public Producto obtener(@PathVariable Long id) {
        return productoService.buscarPorId(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto " + id + " no encontrado"));
    }

    @PostMapping
    public ResponseEntity<Producto> crear(@Valid @RequestBody CrearProductoRequest request) {
        Producto creado = productoService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }
}
