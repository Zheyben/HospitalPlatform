package com.hospital.platform.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import java.util.Set;
import com.hospital.platform.users.service.AuthenticatedUser;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import java.time.LocalDateTime;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
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
    @Autowired private DataSource dataSource;

    @BeforeEach
    void clean() {
        jdbcTemplate.execute("truncate table clinical_records, patients, refresh_tokens, "
                + "user_roles, users restart identity cascade");
    }

    @Test
    void registersPatientAtomicallyWithPatientRoleAndAllowsSeparateLogin() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request("patient@example.com", "12345678")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("patient@example.com"))
                .andExpect(jsonPath("$.firstName").value("Ana"))
                .andExpect(jsonPath("$.insurance").value("SIS"))
                .andExpect(jsonPath("$.insuranceId").value("a0000000-0000-4000-8000-000000000001"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist())
                .andExpect(jsonPath("$.accessToken").doesNotExist());

        assertThat(flyway.info().current().getVersion().toString()).isEqualTo("18");
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
        )).isEqualTo("SIS");
        assertThat(jdbcTemplate.queryForObject(
                "select insurance_id from patients where document_number = ?", UUID.class, "12345678"
        )).isEqualTo(UUID.fromString("a0000000-0000-4000-8000-000000000001"));
        assertThat(jdbcTemplate.queryForObject(
                "select address from patients where document_number = ?", String.class, "12345678"
        )).isNull();
        assertThat(jdbcTemplate.queryForObject(
                "select sex from patients where document_number = ?", String.class, "12345678"
        )).isNull();
        assertThat(jdbcTemplate.queryForObject(
                "select count(*) from clinical_records cr join patients p on p.id=cr.patient_id "
                        + "where p.document_number=?", Integer.class, "12345678"
        )).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject(
                "select record_number from clinical_records cr join patients p on p.id=cr.patient_id "
                        + "where p.document_number=?", String.class, "12345678"
        )).startsWith("HC-");
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
    void exposesOnlyApprovedOptionsAndAcceptsIdWithoutLegacyText() throws Exception {
        mockMvc.perform(get("/insurance-providers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));

        String byId = request("id@example.test", "12345678")
                .replace("\"insurance\":\"SIS\"",
                        "\"insuranceId\":\"a0000000-0000-4000-8000-000000000002\"");
        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON).content(byId))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.insurance").value("EsSalud"))
                .andExpect(jsonPath("$.insuranceId").value("a0000000-0000-4000-8000-000000000002"));

        assertThat(jdbcTemplate.queryForObject(
                "select insurance from patients where document_number = '12345678'", String.class
        )).isEqualTo("EsSalud");

        String withBlankLegacy = request("blank-legacy@example.test", "87654321")
                .replace("\"insurance\":\"SIS\"",
                        "\"insurance\":\"   \",\"insuranceId\":\"a0000000-0000-4000-8000-000000000003\"");
        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(withBlankLegacy))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.insurance").value("Particular"));
    }

    @Test
    void rejectsUnknownOrContradictoryInsuranceWithoutCreatingAccount() throws Exception {
        String unknown = request("unknown@example.test", "12345678")
                .replace("\"insurance\":\"SIS\"", "\"insurance\":\"Unknown Plan\"");
        String contradictory = request("contradictory@example.test", "87654321")
                .replace("\"insurance\":\"SIS\"",
                        "\"insurance\":\"SIS\",\"insuranceId\":\"a0000000-0000-4000-8000-000000000002\"");
        for (String invalid : new String[] {unknown, contradictory}) {
            mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON).content(invalid))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
        }
        assertThat(jdbcTemplate.queryForObject("select count(*) from users", Integer.class)).isZero();
    }

    @Test
    void databaseNormalizesLegacyWritesAndRejectsUnknownProviderIds() {
        UUID patientId = UUID.randomUUID();
        jdbcTemplate.update("insert into patients (id, document_type, document_number, insurance) "
                + "values (?, 'DNI', '93000001', '  sis  ')", patientId);
        assertThat(jdbcTemplate.queryForObject("select insurance_id from patients where id = ?",
                UUID.class, patientId))
                .isEqualTo(UUID.fromString("a0000000-0000-4000-8000-000000000001"));
        assertThat(jdbcTemplate.queryForObject("select insurance from patients where id = ?",
                String.class, patientId)).isEqualTo("SIS");

        assertThatThrownBy(() -> jdbcTemplate.update(
                "insert into patients (id, document_type, document_number, insurance_id) "
                        + "values (?, 'DNI', '93000002', ?)", UUID.randomUUID(), UUID.randomUUID()))
                .isInstanceOf(org.springframework.dao.DataAccessException.class);
        assertThat(jdbcTemplate.queryForObject("select count(*) from patients", Integer.class)).isEqualTo(1);
    }

    @Test
    void backfillsKnownInsuranceAndRejectsUnknownLegacyValue() {
        String schema = "insurance_upgrade_test";
        jdbcTemplate.execute("drop schema if exists " + schema + " cascade");
        try {
            Flyway old = Flyway.configure().dataSource(dataSource).locations("classpath:db/migration")
                    .schemas(schema).defaultSchema(schema).createSchemas(true).target("16").load();
            old.migrate();
            jdbcTemplate.update("insert into " + schema + ".patients "
                    + "(id, document_type, document_number, insurance) values (?, 'DNI', '91000001', '  essalud  ')",
                    UUID.randomUUID());
            Flyway latest = Flyway.configure().dataSource(dataSource).locations("classpath:db/migration")
                    .schemas(schema).defaultSchema(schema).createSchemas(true).load();
            latest.migrate();
            assertThat(jdbcTemplate.queryForObject("select insurance from " + schema
                    + ".patients where document_number = '91000001'", String.class)).isEqualTo("EsSalud");
            assertThat(jdbcTemplate.queryForObject("select insurance_id from " + schema
                    + ".patients where document_number = '91000001'", UUID.class))
                    .isEqualTo(UUID.fromString("a0000000-0000-4000-8000-000000000002"));
        } finally {
            jdbcTemplate.execute("drop schema if exists " + schema + " cascade");
        }

        String badSchema = "insurance_unknown_upgrade_test";
        jdbcTemplate.execute("drop schema if exists " + badSchema + " cascade");
        try {
            Flyway old = Flyway.configure().dataSource(dataSource).locations("classpath:db/migration")
                    .schemas(badSchema).defaultSchema(badSchema).createSchemas(true).target("16").load();
            old.migrate();
            jdbcTemplate.update("insert into " + badSchema + ".patients "
                    + "(id, document_type, document_number, insurance) values (?, 'DNI', '92000001', 'Unknown Plan')",
                    UUID.randomUUID());
            Flyway latest = Flyway.configure().dataSource(dataSource).locations("classpath:db/migration")
                    .schemas(badSchema).defaultSchema(badSchema).createSchemas(true).load();
            assertThatThrownBy(latest::migrate).isInstanceOf(FlywayException.class);
            assertThat(jdbcTemplate.queryForObject("select insurance from " + badSchema
                    + ".patients where document_number = '92000001'", String.class)).isEqualTo("Unknown Plan");
        } finally {
            jdbcTemplate.execute("drop schema if exists " + badSchema + " cascade");
        }
    }

    @Test
    void acceptsOptionalAddressWithoutChangingNineFieldRegistration() throws Exception {
        String withAddress = request("address@example.test", "12345678")
                .replace("\"insurance\":\"SIS\"",
                        "\"insurance\":\"SIS\",\"address\":\"  Avenida Lima 123  \"");
        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON).content(withAddress))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.address").value("Avenida Lima 123"));
        assertThat(jdbcTemplate.queryForObject(
                "select address from patients where document_number = ?", String.class, "12345678"
        )).isEqualTo("Avenida Lima 123");

        String blankAddress = request("blank-address@example.test", "87654321")
                .replace("\"insurance\":\"SIS\"",
                        "\"insurance\":\"SIS\",\"address\":\"   \"");
        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON).content(blankAddress))
                .andExpect(status().isCreated());
        assertThat(jdbcTemplate.queryForObject(
                "select address from patients where document_number = ?", String.class, "87654321"
        )).isNull();

        String oversizedAddress = request("long-address@example.test", "99999999")
                .replace("\"insurance\":\"SIS\"",
                        "\"insurance\":\"SIS\",\"address\":\"" + "a".repeat(501) + "\"");
        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(oversizedAddress))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
        assertThat(jdbcTemplate.queryForObject(
                "select count(*) from users where email = 'long-address@example.test'", Integer.class
        )).isZero();
    }

    @Test
    void acceptsOptionalSexAndRejectsOversizedValue() throws Exception {
        String withSex = request("sex@example.test", "12345678")
                .replace("\"insurance\":\"SIS\"",
                        "\"insurance\":\"SIS\",\"sex\":\"  Femenino  \"");
        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON).content(withSex))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sex").value("Femenino"));
        assertThat(jdbcTemplate.queryForObject(
                "select sex from patients where document_number = ?", String.class, "12345678"
        )).isEqualTo("Femenino");

        String blankSex = request("blank-sex@example.test", "87654321")
                .replace("\"insurance\":\"SIS\"",
                        "\"insurance\":\"SIS\",\"sex\":\"   \"");
        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON).content(blankSex))
                .andExpect(status().isCreated());
        assertThat(jdbcTemplate.queryForObject(
                "select sex from patients where document_number = ?", String.class, "87654321"
        )).isNull();

        String oversizedSex = request("long-sex@example.test", "99999999")
                .replace("\"insurance\":\"SIS\"",
                        "\"insurance\":\"SIS\",\"sex\":\"" + "a".repeat(51) + "\"");
        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON).content(oversizedSex))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
        assertThat(jdbcTemplate.queryForObject(
                "select count(*) from users where email = 'long-sex@example.test'", Integer.class
        )).isZero();
    }

    @Test
    void registersOptionalDemographicsAndAllowsOnlyAdminToReplaceThem() throws Exception {
        String request = request("demographics@example.test", "12345678")
                .replace("\"insurance\":\"SIS\"",
                        "\"insurance\":\"SIS\",\"maritalStatus\":\"  Soltera  \","
                                + "\"occupation\":\" Docente \",\"district\":\" Lima \","
                                + "\"educationLevel\":\" Superior \",\"affiliationNumber\":\" 123-45 \","
                                + "\"emergencyContactName\":\" Persona Sintetica \","
                                + "\"emergencyContactRelationship\":\" Hermana \","
                                + "\"emergencyContactPhone\":\"+51987654321\"");
        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON).content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.maritalStatus").value("Soltera"))
                .andExpect(jsonPath("$.affiliationNumber").value("123-45"));
        UUID patientId = jdbcTemplate.queryForObject(
                "select id from patients where document_number = '12345678'", UUID.class);
        UUID userId = jdbcTemplate.queryForObject(
                "select user_id from patients where id = ?", UUID.class, patientId);
        mockMvc.perform(get("/patients/me").with(authentication(actor(userId, "PATIENT"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.district").value("Lima"))
                .andExpect(jsonPath("$.emergencyContactPhone").value("+51987654321"));
        String replacement = "{\"maritalStatus\":\"Casada\",\"affiliationNumber\":\"678-90\"}";
        mockMvc.perform(patch("/patients/{id}/demographics", patientId)
                        .with(authentication(actor(userId, "PATIENT")))
                        .contentType(MediaType.APPLICATION_JSON).content(replacement))
                .andExpect(status().isForbidden());
        mockMvc.perform(patch("/patients/{id}/demographics", patientId)
                        .with(authentication(actor(UUID.randomUUID(), "ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON).content(replacement))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.maritalStatus").value("Casada"))
                .andExpect(jsonPath("$.occupation").isEmpty());
        assertThat(jdbcTemplate.queryForObject(
                "select occupation from patients where id = ?", String.class, patientId)).isNull();
    }

    @Test
    void rejectsParticularAffiliationAndInvalidEmergencyPhoneBeforeCreatingAccount() throws Exception {
        String particular = request("particular@example.test", "12345678")
                .replace("\"insurance\":\"SIS\"",
                        "\"insurance\":\"Particular\",\"affiliationNumber\":\"ABC\"");
        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON).content(particular))
                .andExpect(status().isBadRequest());
        String invalidPhone = request("invalid-emergency@example.test", "87654321")
                .replace("\"insurance\":\"SIS\"",
                        "\"insurance\":\"SIS\",\"emergencyContactPhone\":\"invalid\"");
        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON).content(invalidPhone))
                .andExpect(status().isBadRequest());
        assertThat(jdbcTemplate.queryForObject("select count(*) from users", Integer.class)).isZero();
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
        assertThat(jdbcTemplate.queryForObject("select count(*) from clinical_records", Integer.class)).isEqualTo(1);
    }

    @Test
    void upgradesLegacyPatientsAndCreatesHeadersForLaterInserts() {
        String schema = "clinical_record_upgrade_test";
        jdbcTemplate.execute("drop schema if exists " + schema + " cascade");
        try {
            Flyway v6 = Flyway.configure().dataSource(dataSource)
                    .locations("classpath:db/migration").schemas(schema).defaultSchema(schema)
                    .createSchemas(true).target("6").load();
            v6.migrate();
            LocalDateTime registeredAt = LocalDateTime.of(2026, 9, 1, 10, 30);
            UUID legacyPatientId = UUID.randomUUID();
            jdbcTemplate.update("insert into " + schema + ".patients "
                            + "(id, document_type, document_number, created_at) values (?, 'DNI', ?, ?)",
                    legacyPatientId, "91000001", registeredAt);

            Flyway latest = Flyway.configure().dataSource(dataSource)
                    .locations("classpath:db/migration").schemas(schema).defaultSchema(schema)
                    .createSchemas(true).load();
            latest.migrate();

            assertThat(latest.info().current().getVersion().toString()).isEqualTo("18");
            assertThat(jdbcTemplate.queryForObject(
                    "select sex from " + schema + ".patients where id = ?", String.class, legacyPatientId
            )).isNull();
            assertThat(jdbcTemplate.queryForObject(
                    "select marital_status from " + schema + ".patients where id = ?", String.class,
                    legacyPatientId)).isNull();
            assertThat(jdbcTemplate.queryForObject(
                    "select opened_at from " + schema + ".clinical_records where patient_id=?",
                    LocalDateTime.class, legacyPatientId)).isEqualTo(registeredAt);
            String legacyNumber = jdbcTemplate.queryForObject(
                    "select record_number from " + schema + ".clinical_records where patient_id=?",
                    String.class, legacyPatientId);
            assertThat(legacyNumber).startsWith("HC-");

            UUID newPatientId = UUID.randomUUID();
            jdbcTemplate.update("insert into " + schema + ".patients "
                            + "(id, document_type, document_number) values (?, 'DNI', ?)",
                    newPatientId, "91000002");
            assertThat(jdbcTemplate.queryForObject(
                    "select record_number from " + schema + ".clinical_records where patient_id=?",
                    String.class, newPatientId)).isNotEqualTo(legacyNumber);
            assertThat(jdbcTemplate.queryForObject(
                    "select count(*) from " + schema + ".clinical_records", Integer.class)).isEqualTo(2);
        } finally {
            jdbcTemplate.execute("drop schema if exists " + schema + " cascade");
        }
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
                 "birthDate":"1990-01-01","phone":"3001234567","insurance":"SIS"}
                """.formatted(email, documentNumber);
    }

    private UsernamePasswordAuthenticationToken actor(UUID userId, String role) {
        AuthenticatedUser user = new AuthenticatedUser(userId, "synthetic@example.test", "synthetic",
                "test", true, Set.of(role), Set.of());
        return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    }
}
