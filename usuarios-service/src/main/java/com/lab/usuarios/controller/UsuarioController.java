package com.lab.usuarios.controller;

import com.lab.usuarios.dto.CrearUsuarioRequest;
import com.lab.usuarios.exception.ResourceNotFoundException;
import com.lab.usuarios.model.Usuario;
import com.lab.usuarios.service.UsuarioService;
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
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public List<Usuario> listar() {
        return usuarioService.listar();
    }

    @GetMapping("/{id}")
    public Usuario obtener(@PathVariable Long id) {
        return usuarioService.buscarPorId(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario " + id + " no encontrado"));
    }

    @PostMapping
    public ResponseEntity<Usuario> crear(@Valid @RequestBody CrearUsuarioRequest request) {
        Usuario creado = usuarioService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }
}
