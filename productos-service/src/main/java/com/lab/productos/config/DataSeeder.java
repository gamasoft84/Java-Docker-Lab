package com.lab.productos.config;

import com.lab.productos.model.Producto;
import com.lab.productos.repository.ProductoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    private final ProductoRepository productoRepository;

    public DataSeeder(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    @Override
    public void run(String... args) {
        // Solo carga datos de ejemplo si la tabla esta vacia
        if (productoRepository.count() > 0) {
            return;
        }
        productoRepository.saveAll(List.of(
                new Producto("Teclado mecanico", 45.99, 20),
                new Producto("Mouse inalambrico", 19.50, 50)
        ));
    }
}
