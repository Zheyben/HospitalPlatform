package com.hospital.platform.auth.controller;

import com.hospital.platform.auth.dto.LoginRequestDTO;
import com.hospital.platform.auth.dto.LoginResponseDTO;
import com.hospital.platform.auth.dto.RegisterPatientRequestDTO;
import com.hospital.platform.auth.dto.RegisterPatientResponseDTO;
import com.hospital.platform.auth.dto.RefreshTokenRequestDTO;
import com.hospital.platform.auth.service.AuthService;
import com.hospital.platform.auth.service.PatientRegistrationService;
import com.hospital.platform.users.service.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final PatientRegistrationService patientRegistrationService;

    public AuthController(AuthService authService, PatientRegistrationService patientRegistrationService) {
        this.authService = authService;
        this.patientRegistrationService = patientRegistrationService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterPatientResponseDTO> register(
            @Valid @RequestBody RegisterPatientRequestDTO request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(patientRegistrationService.register(request));
    }

    @PostMapping("/login")
    public LoginResponseDTO login(@Valid @RequestBody LoginRequestDTO request) {
        return authService.login(request);
    }

    @PostMapping("/refresh")
    public LoginResponseDTO refresh(@Valid @RequestBody RefreshTokenRequestDTO request) {
        return authService.refresh(request);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@AuthenticationPrincipal AuthenticatedUser user) {
        authService.logout(user);
    }
}
