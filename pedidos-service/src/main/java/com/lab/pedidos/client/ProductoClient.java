package com.lab.pedidos.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.Optional;

@Component
public class ProductoClient {

    private final RestClient restClient;

    public ProductoClient(@Qualifier("productosRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public Optional<ProductoResponse> buscarPorId(Long productoId) {
        try {
            ProductoResponse producto = restClient.get()
                    .uri("/productos/{id}", productoId)
                    .retrieve()
                    .body(ProductoResponse.class);
            return Optional.ofNullable(producto);
        } catch (HttpClientErrorException e) {
            // Si productos-service responde 404, el producto no existe -> Optional vacio.
            // Cualquier otro error 4xx se propaga para no ocultarlo.
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                return Optional.empty();
            }
            throw e;
        }
    }
}
