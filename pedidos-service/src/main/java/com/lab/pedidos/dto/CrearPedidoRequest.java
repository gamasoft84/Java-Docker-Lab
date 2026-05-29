package com.lab.pedidos.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class CrearPedidoRequest {

    @NotNull(message = "usuarioId es obligatorio")
    private Long usuarioId;

    @NotNull(message = "productoId es obligatorio")
    private Long productoId;

    @Positive(message = "La cantidad debe ser mayor que 0")
    private int cantidad;

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public Long getProductoId() {
        return productoId;
    }

    public void setProductoId(Long productoId) {
        this.productoId = productoId;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }
}
