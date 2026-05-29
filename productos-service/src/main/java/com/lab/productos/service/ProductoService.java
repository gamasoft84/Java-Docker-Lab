package com.lab.productos.service;

import com.lab.productos.dto.CrearProductoRequest;
import com.lab.productos.model.Producto;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ProductoService {

    private final ConcurrentHashMap<Long, Producto> productos = new ConcurrentHashMap<>();
    private final AtomicLong secuencia = new AtomicLong(0);

    public ProductoService() {
        // Datos de ejemplo en memoria (ids 1 y 2)
        guardar("Teclado mecanico", 45.99, 20);
        guardar("Mouse inalambrico", 19.50, 50);
    }

    private void guardar(String nombre, double precio, int stock) {
        CrearProductoRequest request = new CrearProductoRequest();
        request.setNombre(nombre);
        request.setPrecio(precio);
        request.setStock(stock);
        crear(request);
    }

    public List<Producto> listar() {
        return new ArrayList<>(productos.values());
    }

    public Optional<Producto> buscarPorId(Long id) {
        return Optional.ofNullable(productos.get(id));
    }

    public Producto crear(CrearProductoRequest request) {
        Long id = secuencia.incrementAndGet();
        Producto producto = new Producto(id, request.getNombre(), request.getPrecio(), request.getStock());
        productos.put(id, producto);
        return producto;
    }
}
