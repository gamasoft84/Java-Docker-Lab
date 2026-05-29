package com.lab.pedidos.client;

import org.springframework.beans.factory.annotation.Qualifier;
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
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        }
    }
}
