package com.lab.usuarios.service;

import com.lab.usuarios.dto.CrearUsuarioRequest;
import com.lab.usuarios.model.Usuario;
import com.lab.usuarios.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public List<Usuario> listar() {
        return usuarioRepository.findAll();
    }

    public Optional<Usuario> buscarPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    public Usuario crear(CrearUsuarioRequest request) {
        Usuario usuario = new Usuario(request.getNombre(), request.getEmail());
        return usuarioRepository.save(usuario);
    }
}
