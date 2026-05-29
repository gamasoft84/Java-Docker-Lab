package com.lab.pedidos.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
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
        } catch (HttpClientErrorException e) {
            // Si usuarios-service responde 404, el usuario no existe -> Optional vacio.
            // Cualquier otro error 4xx se propaga para no ocultarlo.
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                return Optional.empty();
            }
            throw e;
        }
    }
}
