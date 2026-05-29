package com.lab.pedidos.service;

import com.lab.pedidos.client.ProductoClient;
import com.lab.pedidos.client.ProductoResponse;
import com.lab.pedidos.client.UsuarioClient;
import com.lab.pedidos.client.UsuarioResponse;
import com.lab.pedidos.dto.CrearPedidoRequest;
import com.lab.pedidos.model.Pedido;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class PedidoService {

    private final ConcurrentHashMap<Long, Pedido> pedidos = new ConcurrentHashMap<>();
    private final AtomicLong secuencia = new AtomicLong(0);

    private final UsuarioClient usuarioClient;
    private final ProductoClient productoClient;

    public PedidoService(UsuarioClient usuarioClient, ProductoClient productoClient) {
        this.usuarioClient = usuarioClient;
        this.productoClient = productoClient;
    }

    public List<Pedido> listar() {
        return new ArrayList<>(pedidos.values());
    }

    public Optional<Pedido> buscarPorId(Long id) {
        return Optional.ofNullable(pedidos.get(id));
    }

    public Pedido crear(CrearPedidoRequest request) {
        // 1) Validar usuario via HTTP contra usuarios-service
        UsuarioResponse usuario = usuarioClient.buscarPorId(request.getUsuarioId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "El usuario " + request.getUsuarioId() + " no existe"));

        // 2) Validar producto via HTTP contra productos-service
        ProductoResponse producto = productoClient.buscarPorId(request.getProductoId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "El producto " + request.getProductoId() + " no existe"));

        // 3) Validar stock disponible
        if (producto.stock() < request.getCantidad()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Stock insuficiente para el producto " + producto.id()
                            + " (disponible: " + producto.stock()
                            + ", solicitado: " + request.getCantidad() + ")");
        }

        // 4) Crear el pedido en memoria
        Long id = secuencia.incrementAndGet();
        Pedido pedido = new Pedido();
        pedido.setId(id);
        pedido.setUsuarioId(usuario.id());
        pedido.setNombreUsuario(usuario.nombre());
        pedido.setProductoId(producto.id());
        pedido.setNombreProducto(producto.nombre());
        pedido.setCantidad(request.getCantidad());
        pedido.setPrecioUnitario(producto.precio());
        pedido.setTotal(producto.precio() * request.getCantidad());
        pedidos.put(id, pedido);
        return pedido;
    }
}
