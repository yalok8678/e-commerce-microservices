package com.ecommerce.authentication.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthResponse {

    private Long userId;
    private String email;
    private String role;
    private String accessToken;
    private String refreshToken;
}