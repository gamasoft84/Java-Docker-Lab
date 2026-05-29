package com.lab.pedidos.client;

public record ProductoResponse(Long id, String nombre, double precio, int stock) {
}
