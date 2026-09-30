package com.ecommerce.gateway.filter;

import com.ecommerce.gateway.security.JwtService;

import lombok.RequiredArgsConstructor;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;

import org.springframework.core.Ordered;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import org.springframework.http.server.reactive.ServerHttpRequest;

import org.springframework.stereotype.Component;

import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter
        implements GlobalFilter, Ordered {

    private final JwtService jwtService;

    @Override
    public Mono<Void> filter(
            ServerWebExchange exchange,
            GatewayFilterChain chain) {

        String path =
                exchange.getRequest()
                        .getURI()
                        .getPath();

        /*
         * Public endpoints
         */
        if (isPublicEndpoint(path)) {

            return chain.filter(exchange);
        }

        /*
         * Read Authorization header
         */
        String authorizationHeader =
                exchange.getRequest()
                        .getHeaders()
                        .getFirst(
                                HttpHeaders.AUTHORIZATION
                        );

        /*
         * Missing token
         */
        if (authorizationHeader == null
                || !authorizationHeader.startsWith(
                "Bearer ")) {

            return unauthorized(exchange);
        }

        String token =
                authorizationHeader.substring(7);

        /*
         * Invalid token
         */
        if (!jwtService.isTokenValid(token)) {

            return unauthorized(exchange);
        }

        Long userId =
                jwtService.extractUserId(token);

        String role =
                jwtService.extractRole(token);

        String subject =
                jwtService.extractSubject(token);

        /*
         * userId must exist
         */
        if (userId == null) {

            return unauthorized(exchange);
        }

        /*
         * Add authenticated user information
         * to downstream request.
         */
        ServerHttpRequest mutatedRequest =
                exchange.getRequest()
                        .mutate()
                        .header(
                                "X-User-Id",
                                userId.toString()
                        )
                        .header(
                                "X-User-Role",
                                role != null
                                        ? role
                                        : ""
                        )
                        .header(
                                "X-User-Subject",
                                subject != null
                                        ? subject
                                        : ""
                        )
                        .build();

        ServerWebExchange mutatedExchange =
                exchange.mutate()
                        .request(mutatedRequest)
                        .build();

        return chain.filter(mutatedExchange);
    }

    private boolean isPublicEndpoint(
            String path) {

        return path.equals(
                "/actuator/health"
        )
                || path.equals(
                "/api/auth/login"
        )
                || path.equals(
                "/api/auth/register"
        );
    }

    private Mono<Void> unauthorized(
            ServerWebExchange exchange) {

        exchange.getResponse()
                .setStatusCode(
                        HttpStatus.UNAUTHORIZED
                );

        return exchange.getResponse()
                .setComplete();
    }

    @Override
    public int getOrder() {

        return -100;
    }
}