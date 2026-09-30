package com.ecommerce.authentication.service;

import com.ecommerce.authentication.dto.AuthResponse;
import com.ecommerce.authentication.dto.LoginRequest;
import com.ecommerce.authentication.dto.RegisterRequest;
import com.ecommerce.authentication.entity.RefreshToken;
import com.ecommerce.authentication.entity.User;
import com.ecommerce.authentication.repository.RefreshTokenRepository;
import com.ecommerce.authentication.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final RefreshTokenRepository refreshTokenRepository;

    public AuthResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role("USER")
                .build();

        User savedUser = userRepository.save(user);

        return new AuthResponse(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getRole(),
                null,
                null
        );
    }

    public AuthResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new RuntimeException("Invalid email or password"));

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword())) {

            throw new RuntimeException("Invalid email or password");
        }

        String accessToken = jwtService.generateToken(
                user.getId(),
                user.getEmail(),
                user.getRole()
        );

        RefreshToken refreshToken =
                refreshTokenService.createRefreshToken(user.getId());

        return new AuthResponse(
                user.getId(),
                user.getEmail(),
                user.getRole(),
                accessToken,
                refreshToken.getToken()
        );
    }

    public AuthResponse refreshAccessToken(
            String refreshTokenValue) {

        RefreshToken refreshToken = refreshTokenRepository
                .findByToken(refreshTokenValue)
                .orElseThrow(() ->
                        new RuntimeException("Invalid refresh token"));

        refreshTokenService.verifyExpiration(refreshToken);

        User user = userRepository.findById(
                        refreshToken.getUserId())
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        // Rotate refresh token
        RefreshToken newRefreshToken =
                refreshTokenService.createRefreshToken(
                        user.getId());

        String newAccessToken = jwtService.generateToken(
                user.getId(),
                user.getEmail(),
                user.getRole()
        );

        return new AuthResponse(
                user.getId(),
                user.getEmail(),
                user.getRole(),
                newAccessToken,
                newRefreshToken.getToken()
        );
    }

    public void logout(String refreshToken) {
        refreshTokenService.deleteByToken(refreshToken);
    }
}