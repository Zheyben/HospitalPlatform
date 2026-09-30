package com.hospital.platform.auth.service;

import com.hospital.platform.auth.dto.LoginRequestDTO;
import com.hospital.platform.auth.dto.LoginResponseDTO;
import com.hospital.platform.auth.dto.RefreshTokenRequestDTO;
import com.hospital.platform.auth.entity.RefreshToken;
import com.hospital.platform.auth.mapper.AuthTokenMapper;
import com.hospital.platform.security.config.JwtProperties;
import com.hospital.platform.security.jwt.JwtService;
import com.hospital.platform.users.service.AuthenticatedUser;
import com.hospital.platform.users.service.PlatformUserDetailsService;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final PlatformUserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final AuthTokenMapper authTokenMapper;
    private final JwtProperties jwtProperties;

    public AuthService(
            AuthenticationManager authenticationManager,
            PlatformUserDetailsService userDetailsService,
            JwtService jwtService,
            RefreshTokenService refreshTokenService,
            AuthTokenMapper authTokenMapper,
            JwtProperties jwtProperties
    ) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.authTokenMapper = authTokenMapper;
        this.jwtProperties = jwtProperties;
    }

    public LoginResponseDTO login(LoginRequestDTO request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        AuthenticatedUser user = authenticatedUser(authentication, request.email());
        assertUserEnabled(user);

        String accessToken = jwtService.generateAccessToken(user);
        CreatedRefreshToken refreshToken = refreshTokenService.issueToken(user);

        return authTokenMapper.toLoginResponse(accessToken, refreshToken, jwtProperties.jwtExpiration());
    }

    @Transactional
    public LoginResponseDTO refresh(RefreshTokenRequestDTO request) {
        RefreshToken token = refreshTokenService.consumeToken(request.refreshToken());
        AuthenticatedUser user = userDetailsService.loadUserById(token.getUser().getId());
        assertUserEnabled(user);

        String accessToken = jwtService.generateAccessToken(user);
        CreatedRefreshToken refreshToken = refreshTokenService.issueToken(user);

        return authTokenMapper.toLoginResponse(accessToken, refreshToken, jwtProperties.jwtExpiration());
    }

    public void logout(AuthenticatedUser user) {
        if (user != null) {
            refreshTokenService.revokeActiveTokens(user.id());
        }
    }

    private AuthenticatedUser authenticatedUser(Authentication authentication, String email) {
        if (authentication.getPrincipal() instanceof AuthenticatedUser user) {
            return user;
        }
        return userDetailsService.loadUserByUsername(email);
    }

    private void assertUserEnabled(AuthenticatedUser user) {
        if (!user.isEnabled()) {
            throw new DisabledException("User is disabled");
        }
    }
}
