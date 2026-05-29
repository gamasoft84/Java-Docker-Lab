package com.lab.pedidos.service;

import com.lab.pedidos.client.ProductoClient;
import com.lab.pedidos.client.ProductoResponse;
import com.lab.pedidos.client.UsuarioClient;
import com.lab.pedidos.client.UsuarioResponse;
import com.lab.pedidos.dto.CrearPedidoRequest;
import com.lab.pedidos.exception.BusinessException;
import com.lab.pedidos.exception.ResourceNotFoundException;
import com.lab.pedidos.model.Pedido;
import com.lab.pedidos.repository.PedidoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final UsuarioClient usuarioClient;
    private final ProductoClient productoClient;

    public PedidoService(PedidoRepository pedidoRepository,
                         UsuarioClient usuarioClient,
                         ProductoClient productoClient) {
        this.pedidoRepository = pedidoRepository;
        this.usuarioClient = usuarioClient;
        this.productoClient = productoClient;
    }

    public List<Pedido> listar() {
        return pedidoRepository.findAll();
    }

    public Optional<Pedido> buscarPorId(Long id) {
        return pedidoRepository.findById(id);
    }

    public Pedido crear(CrearPedidoRequest request) {
        // 1) Validar usuario via HTTP contra usuarios-service
        UsuarioResponse usuario = usuarioClient.buscarPorId(request.getUsuarioId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuario no encontrado con id " + request.getUsuarioId()));

        // 2) Validar producto via HTTP contra productos-service
        ProductoResponse producto = productoClient.buscarPorId(request.getProductoId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Producto no encontrado con id " + request.getProductoId()));

        // 3) Validar stock disponible (regla de negocio)
        if (producto.stock() < request.getCantidad()) {
            throw new BusinessException(
                    "Stock insuficiente para el producto " + producto.id()
                            + ". Stock disponible: " + producto.stock()
                            + ", cantidad solicitada: " + request.getCantidad());
        }

        // 4) Guardar el pedido en la base de datos (el id lo genera Postgres)
        Pedido pedido = new Pedido();
        pedido.setUsuarioId(usuario.id());
        pedido.setNombreUsuario(usuario.nombre());
        pedido.setProductoId(producto.id());
        pedido.setNombreProducto(producto.nombre());
        pedido.setCantidad(request.getCantidad());
        pedido.setPrecioUnitario(producto.precio());
        pedido.setTotal(producto.precio() * request.getCantidad());
        return pedidoRepository.save(pedido);
    }
}
