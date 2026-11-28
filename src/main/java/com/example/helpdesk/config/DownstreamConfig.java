package com.example.helpdesk.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class DownstreamConfig {

    @Bean
    public RestClient downstreamClient(
            DownstreamProperties properties
    ) {
        SimpleClientHttpRequestFactory factory =
                new SimpleClientHttpRequestFactory();

        factory.setConnectTimeout(properties.timeout());
        factory.setReadTimeout(properties.timeout());

        return RestClient.builder()
                .baseUrl(properties.baseUrl().toString())
                .requestFactory(factory)
                .build();
    }
}