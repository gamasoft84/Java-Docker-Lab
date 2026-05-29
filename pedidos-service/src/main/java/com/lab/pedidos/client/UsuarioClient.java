package com.lab.pedidos.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.Optional;

@Component
public class UsuarioClient {

    private final RestClient restClient;

    public UsuarioClient(@Qualifier("usuariosRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public Optional<UsuarioResponse> buscarPorId(Long usuarioId) {
        try {
            UsuarioResponse usuario = restClient.get()
                    .uri("/usuarios/{id}", usuarioId)
                    .retrieve()
                    .body(UsuarioResponse.class);
            return Optional.ofNullable(usuario);
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        }
    }
}
