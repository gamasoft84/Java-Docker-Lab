package com.lab.usuarios.config;

import com.lab.usuarios.model.Usuario;
import com.lab.usuarios.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;

    public DataSeeder(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public void run(String... args) {
        // Solo carga datos de ejemplo si la tabla esta vacia
        if (usuarioRepository.count() > 0) {
            return;
        }
        usuarioRepository.saveAll(List.of(
                new Usuario("Ana Lopez", "ana@example.com"),
                new Usuario("Bruno Diaz", "bruno@example.com"),
                new Usuario("Rick Garcia", "rickgarcia@example.com"),
                new Usuario("Dafne Juarez", "dafnejuarez@example.com")
        ));
    }
}
