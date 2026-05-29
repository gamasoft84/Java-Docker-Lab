package com.lab.pedidos.controller;

import com.lab.pedidos.dto.CrearPedidoRequest;
import com.lab.pedidos.exception.ResourceNotFoundException;
import com.lab.pedidos.model.Pedido;
import com.lab.pedidos.service.PedidoService;
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
@RequestMapping("/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @GetMapping
    public List<Pedido> listar() {
        return pedidoService.listar();
    }

    @GetMapping("/{id}")
    public Pedido obtener(@PathVariable Long id) {
        return pedidoService.buscarPorId(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido " + id + " no encontrado"));
    }

    @PostMapping
    public ResponseEntity<Pedido> crear(@Valid @RequestBody CrearPedidoRequest request) {
        Pedido creado = pedidoService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }
}
