package com.hospital.platform.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.hospital.platform.security.config.JwtProperties;
import com.hospital.platform.security.jwt.JwtService;
import com.hospital.platform.users.service.AuthenticatedUser;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@AutoConfigureMockMvc
class AuthenticationSecurityIT {

    private static final UUID USER_ID = UUID.fromString("77777777-7777-7777-7777-777777777771");
    private static final UUID MISSING_USER_ID = UUID.fromString("77777777-7777-7777-7777-777777777772");
    private static final String JWT_SECRET = "authentication-security-integration-test-secret";

    @Container
    static final PostgreSQLContainer postgres = new PostgreSQLContainer(
            DockerImageName.parse("postgres:16-alpine"))
            .withDatabaseName("hospital_platform_authentication_security_test")
            .withUsername("hospital_app_test")
            .withPassword("hospital_app_test");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("hospital.security.jwt-secret", () -> JWT_SECRET);
    }

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void setUpUser() {
        jdbcTemplate.update("delete from users where id = ?", USER_ID);
        jdbcTemplate.update(
                "insert into users (id, username, email, password_hash, enabled) values (?, ?, ?, ?, true)",
                USER_ID,
                "jwt-security-user",
                "jwt-security-user@example.test",
                "test-password-hash"
        );
    }

    @Test
    void activeUserWithValidJwtCanAccessAuthenticatedEndpoint() throws Exception {
        mockMvc.perform(get("/users/me")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(tokenFor(USER_ID))))
                .andExpect(status().isOk());
    }

    @Test
    void disabledUserCannotUsePreviouslyIssuedValidJwt() throws Exception {
        String token = tokenFor(USER_ID);
        jdbcTemplate.update("update users set enabled = false where id = ?", USER_ID);

        mockMvc.perform(get("/users/me")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(token)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void missingUserCannotUseOtherwiseValidJwt() throws Exception {
        mockMvc.perform(get("/users/me")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(tokenFor(MISSING_USER_ID))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void invalidJwtIsRejected() throws Exception {
        mockMvc.perform(get("/users/me")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken("invalid-token")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void expiredJwtIsRejectedByAuthenticatedEndpoint() throws Exception {
        Instant issuedAt = Instant.now().minus(jwtProperties.jwtExpiration()).minusSeconds(1);
        JwtService expiredTokenIssuer = new JwtService(
                jwtProperties,
                Clock.fixed(issuedAt, ZoneOffset.UTC)
        );
        String expiredToken = expiredTokenIssuer.generateAccessToken(authenticatedUser(USER_ID));

        mockMvc.perform(get("/users/me")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(expiredToken)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void softDeletedUserCannotUsePreviouslyIssuedValidJwt() throws Exception {
        String token = tokenFor(USER_ID);

        mockMvc.perform(get("/users/me")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(token)))
                .andExpect(status().isOk());

        jdbcTemplate.update("update users set deleted_at = current_timestamp where id = ?", USER_ID);

        mockMvc.perform(get("/users/me")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(token)))
                .andExpect(status().isUnauthorized());
    }

    private String tokenFor(UUID userId) {
        return jwtService.generateAccessToken(authenticatedUser(userId));
    }

    private AuthenticatedUser authenticatedUser(UUID userId) {
        return new AuthenticatedUser(
                userId,
                "jwt-security-user@example.test",
                "jwt-security-user",
                "test-password-hash",
                true,
                Set.of(),
                Set.of()
        );
    }

    private String bearerToken(String token) {
        return "Bearer " + token;
    }
}
