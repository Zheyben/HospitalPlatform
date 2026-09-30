package com.hospital.platform.auth.mapper;

import com.hospital.platform.auth.dto.LoginResponseDTO;
import com.hospital.platform.auth.service.CreatedRefreshToken;
import java.time.Duration;
import org.springframework.stereotype.Component;

@Component
public class AuthTokenMapper {

    public LoginResponseDTO toLoginResponse(
            String accessToken,
            CreatedRefreshToken refreshToken,
            Duration accessTokenExpiration
    ) {
        return new LoginResponseDTO(
                accessToken,
                refreshToken.token(),
                accessTokenExpiration.toSeconds()
        );
    }
}
