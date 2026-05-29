package com.lab.pedidos.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class ClientesConfig {

    @Bean
    public RestClient usuariosRestClient(
            RestClient.Builder builder,
            @Value("${servicios.usuarios.url}") String usuariosUrl) {
        return builder.baseUrl(usuariosUrl).build();
    }

    @Bean
    public RestClient productosRestClient(
            RestClient.Builder builder,
            @Value("${servicios.productos.url}") String productosUrl) {
        return builder.baseUrl(productosUrl).build();
    }
}
