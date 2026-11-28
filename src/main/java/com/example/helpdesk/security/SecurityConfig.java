package com.example.helpdesk.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.http.HttpMethod;

import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {

        JwtGrantedAuthoritiesConverter roles =
                new JwtGrantedAuthoritiesConverter();

        roles.setAuthoritiesClaimName("roles");
        roles.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter converter =
                new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(roles);

        return converter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            SecurityErrorHandler errors,
            JwtAuthenticationConverter converter
    ) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)

                .formLogin(AbstractHttpConfigurer::disable)

                .httpBasic(AbstractHttpConfigurer::disable)

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/auth/login"
                        ).permitAll()

                        .requestMatchers(
                                "/actuator/health",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/",
                                "/index.html",
                                "/style.css",
                                "/app.js",
                                "/favicon.ico"
                        ).permitAll()

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/tickets/*/start"
                        ).hasRole("SENIOR")

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/tickets"
                        ).hasAnyRole("JUNIOR", "SENIOR")

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/tickets/*",
                                "/api/me"
                        ).hasAnyRole("JUNIOR", "SENIOR")

                        .anyRequest().denyAll()
                )

                .exceptionHandling(ex -> ex

                        .authenticationEntryPoint(
                                (request, response, exception) ->
                                        errors.writeError(
                                                request,
                                                response,
                                                401,
                                                "UNAUTHENTICATED",
                                                "valid token required"
                                        )
                        )

                        .accessDeniedHandler(
                                (request, response, exception) ->
                                        errors.writeError(
                                                request,
                                                response,
                                                403,
                                                "FORBIDDEN",
                                                "not allowed for your role"
                                        )
                        )
                )

                .oauth2ResourceServer(oauth -> oauth

                        .jwt(jwt ->
                                jwt.jwtAuthenticationConverter(converter)
                        )

                        .authenticationEntryPoint(
                                (request, response, exception) ->
                                        errors.writeError(
                                                request,
                                                response,
                                                401,
                                                "UNAUTHENTICATED",
                                                "valid token required"
                                        )
                        )

                        .accessDeniedHandler(
                                (request, response, exception) ->
                                        errors.writeError(
                                                request,
                                                response,
                                                403,
                                                "FORBIDDEN",
                                                "not allowed for your role"
                                        )
                        )
                );

        return http.build();
    }
}