package com.lab.productos.service;

import com.lab.productos.dto.CrearProductoRequest;
import com.lab.productos.model.Producto;
import com.lab.productos.repository.ProductoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;

    public ProductoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    public List<Producto> listar() {
        return productoRepository.findAll();
    }

    public Optional<Producto> buscarPorId(Long id) {
        return productoRepository.findById(id);
    }

    public Producto crear(CrearProductoRequest request) {
        Producto producto = new Producto(request.getNombre(), request.getPrecio(), request.getStock());
        return productoRepository.save(producto);
    }
}
