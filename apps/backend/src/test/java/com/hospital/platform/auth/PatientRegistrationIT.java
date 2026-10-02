package com.hospital.platform.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
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
@AutoConfigureMockMvc
@Testcontainers
class PatientRegistrationIT {

    @Container
    static final PostgreSQLContainer postgres = new PostgreSQLContainer(
            DockerImageName.parse("postgres:16-alpine"))
            .withDatabaseName("hospital_platform_registration_test")
            .withUsername("hospital_app_test")
            .withPassword("hospital_app_test");

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("hospital.security.jwt-secret", () -> "12345678901234567890123456789012");
    }

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbcTemplate;
    @Autowired private Flyway flyway;

    @BeforeEach
    void clean() {
        jdbcTemplate.update("delete from patients");
        jdbcTemplate.update("delete from refresh_tokens");
        jdbcTemplate.update("delete from user_roles");
        jdbcTemplate.update("delete from users");
    }

    @Test
    void registersPatientAtomicallyWithPatientRoleAndAllowsSeparateLogin() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request("patient@example.com", "12345678")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("patient@example.com"))
                .andExpect(jsonPath("$.firstName").value("Ana"))
                .andExpect(jsonPath("$.insurance").value("Demo Health"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist())
                .andExpect(jsonPath("$.accessToken").doesNotExist());

        assertThat(flyway.info().current().getVersion().toString()).isEqualTo("5");
        UUID userId = jdbcTemplate.queryForObject(
                "select id from users where email = ?", UUID.class, "patient@example.com"
        );
        assertThat(userId).isNotNull();
        assertThat(jdbcTemplate.queryForObject(
                "select first_name from users where id = ?", String.class, userId
        )).isEqualTo("Ana");
        assertThat(jdbcTemplate.queryForObject(
                "select password_hash from users where id = ?", String.class, userId
        )).startsWith("$2");
        assertThat(jdbcTemplate.queryForObject(
                "select user_id from patients where document_number = ?", UUID.class, "12345678"
        )).isEqualTo(userId);
        assertThat(jdbcTemplate.queryForObject(
                "select insurance from patients where document_number = ?", String.class, "12345678"
        )).isEqualTo("Demo Health");
        assertThat(jdbcTemplate.queryForList(
                "select r.name from roles r join user_roles ur on ur.role_id = r.id where ur.user_id = ?",
                String.class, userId
        )).containsExactly("PATIENT");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"patient@example.com\",\"password\":\"strong-password\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isString());
    }

    @Test
    void duplicateDocumentRollsBackNewUserAndReturnsConflict() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request("first@example.com", "12345678")))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request("second@example.com", "12345678")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("DUPLICATE_DOCUMENT"));

        assertThat(jdbcTemplate.queryForObject(
                "select count(*) from users where email = ?", Integer.class, "second@example.com"
        )).isZero();
        assertThat(jdbcTemplate.queryForObject("select count(*) from patients", Integer.class)).isEqualTo(1);
    }

    @Test
    void duplicateEmailAndInvalidDataReturnExistingErrorShape() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request("patient@example.com", "12345678")))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request("patient@example.com", "87654321")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("EMAIL_ALREADY_EXISTS"));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request("new@example.com", "99999999")
                                .replace("\"birthDate\":\"1990-01-01\"", "\"birthDate\":\"2990-01-01\"")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }

    @Test
    void enforcesDocumentDomainAndCompositeIdentity() throws Exception {
        for (String invalid : new String[] {
                request("dni-invalid@example.test", "1234567"),
                request("ce-invalid@example.test", "ABC123").replace("\"DNI\"", "\"CE\""),
                request("passport-invalid@example.test", "ABC12").replace("\"DNI\"", "\"PASSPORT\"")
        }) {
            mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON).content(invalid))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
        }

        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(request("dni@example.test", "12345678")))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(request("ce@example.test", "12345678").replace("\"DNI\"", "\"CE\"")))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(request("passport@example.test", "12345678")
                                .replace("\"DNI\"", "\"PASSPORT\"")))
                .andExpect(status().isCreated());
        assertThat(jdbcTemplate.queryForObject("select count(*) from patients where document_number='12345678'",
                Integer.class)).isEqualTo(3);
    }

    private String request(String email, String documentNumber) {
        return """
                {"email":"%s","password":"strong-password","documentType":"DNI",
                 "documentNumber":"%s","firstName":"Ana","lastName":"Pérez",
                 "birthDate":"1990-01-01","phone":"3001234567","insurance":"Demo Health"}
                """.formatted(email, documentNumber);
    }
}
