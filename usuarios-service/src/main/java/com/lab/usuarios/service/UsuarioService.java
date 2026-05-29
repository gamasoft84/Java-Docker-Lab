package com.lab.usuarios.service;

import com.lab.usuarios.dto.CrearUsuarioRequest;
import com.lab.usuarios.model.Usuario;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class UsuarioService {

    private final ConcurrentHashMap<Long, Usuario> usuarios = new ConcurrentHashMap<>();
    private final AtomicLong secuencia = new AtomicLong(0);

    public UsuarioService() {
        // Datos de ejemplo en memoria (ids 1 y 2)
        guardar("Ana Lopez", "ana@example.com");
        guardar("Bruno Diaz", "bruno@example.com");
        guardar("Rick Garcia", "rickgarcia@example.com");
        guardar("Dafne Juarez", "dafnejuarez@example.com");
    }

    private void guardar(String nombre, String email) {
        CrearUsuarioRequest request = new CrearUsuarioRequest();
        request.setNombre(nombre);
        request.setEmail(email);
        crear(request);
    }

    public List<Usuario> listar() {
        return new ArrayList<>(usuarios.values());
    }

    public Optional<Usuario> buscarPorId(Long id) {
        return Optional.ofNullable(usuarios.get(id));
    }

    public Usuario crear(CrearUsuarioRequest request) {
        Long id = secuencia.incrementAndGet();
        Usuario usuario = new Usuario(id, request.getNombre(), request.getEmail());
        usuarios.put(id, usuario);
        return usuario;
    }
}
