package com.hospital.platform.catalogs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.hospital.platform.users.service.AuthenticatedUser;
import java.nio.file.Path;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@AutoConfigureMockMvc
class MedicalCatalogIT {

    private static final UUID USER = UUID.fromString("c0000000-0000-0000-0000-000000000001");

    @Container
    static final PostgreSQLContainer postgres = new PostgreSQLContainer(
            DockerImageName.parse("postgres:16-alpine"))
            .withDatabaseName("hospital_platform_medical_catalog_test")
            .withUsername("hospital_app_test")
            .withPassword("hospital_app_test");

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired private JdbcTemplate jdbc;
    @Autowired private MockMvc mvc;

    @BeforeEach
    void clearCatalogs() {
        jdbc.execute("truncate table medication_presentations, medications, icd10_codes, procedures, "
                + "medical_catalog_sources, users cascade");
    }

    @Test
    void newlyMigratedCatalogsAreEmptyAndProfessionalOnly() throws Exception {
        assertThat(jdbc.queryForObject("select count(*) from medical_catalog_sources", Integer.class)).isZero();
        mvc.perform(get("/medical/catalogs/icd10").param("q", "te")
                        .with(authentication(actor("PROFESSIONAL"))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
        mvc.perform(get("/medical/catalogs/medications").param("q", "te")
                        .with(authentication(actor("PROFESSIONAL"))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
        mvc.perform(get("/medical/catalogs/procedures").param("q", "te")
                        .with(authentication(actor("PROFESSIONAL"))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
        mvc.perform(get("/medical/catalogs/icd10").param("q", "te"))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/medical/catalogs/icd10").param("q", "te")
                        .with(authentication(actor("RECEPTIONIST"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void searchesOnlyActiveRowsAndKeepsPresentationsDependent() throws Exception {
        insertApprover();
        UUID icdSource = source("ICD10");
        UUID medicationSource = source("MEDICATION");
        UUID procedureSource = source("PROCEDURE");
        jdbc.update("insert into icd10_codes (code, description, source_id) values "
                + "('TEST-1', 'Synthetic diagnosis', ?), ('TEST-2', 'Synthetic fever', ?)",
                icdSource, icdSource);
        jdbc.update("insert into icd10_codes (code, description, source_id, active) "
                + "values ('TEST-3', 'Synthetic inactive', ?, false)", icdSource);
        UUID medication = UUID.randomUUID();
        UUID otherMedication = UUID.randomUUID();
        jdbc.update("insert into medications (id, generic_name, commercial_name, source_id) "
                + "values (?, 'Synthetic generic', 'Synthetic brand', ?), "
                + "(?, 'Other synthetic', null, ?)",
                medication, medicationSource, otherMedication, medicationSource);
        jdbc.update("insert into medication_presentations "
                + "(medication_id, name, concentration, pharmaceutical_form) "
                + "values (?, 'Test tablet', '1 test-unit', 'Tablet'), "
                + "(?, 'Other tablet', '2 test-units', 'Tablet')",
                medication, otherMedication);
        jdbc.update("insert into medication_presentations "
                + "(medication_id, name, concentration, pharmaceutical_form, active) "
                + "values (?, 'Inactive tablet', '3 test-units', 'Tablet', false)", medication);
        jdbc.update("insert into procedures (code, name, source_id) "
                + "values ('PROC-TEST', 'Synthetic exam', ?)", procedureSource);
        jdbc.update("insert into procedures (code, name, source_id, active) "
                + "values ('PROC-OFF', 'Synthetic inactive exam', ?, false)", procedureSource);

        mvc.perform(get("/medical/catalogs/icd10").param("q", "SYNTHETIC").param("limit", "1")
                        .with(authentication(actor("PROFESSIONAL"))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
        mvc.perform(get("/medical/catalogs/icd10").param("q", "TEST-")
                        .with(authentication(actor("PROFESSIONAL"))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(2));
        mvc.perform(get("/medical/catalogs/icd10").param("q", "%_")
                        .with(authentication(actor("PROFESSIONAL"))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
        mvc.perform(get("/medical/catalogs/medications").param("q", "BRAND")
                        .with(authentication(actor("PROFESSIONAL"))))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(medication.toString()));
        mvc.perform(get("/medical/catalogs/medications/{id}/presentations", medication)
                        .with(authentication(actor("PROFESSIONAL"))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Test tablet"));
        mvc.perform(get("/medical/catalogs/procedures").param("q", "synthetic")
                        .with(authentication(actor("PROFESSIONAL"))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
        jdbc.update("update medications set active = false where id = ?", medication);
        mvc.perform(get("/medical/catalogs/medications/{id}/presentations", medication)
                        .with(authentication(actor("PROFESSIONAL"))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));

        assertThatThrownBy(() -> jdbc.update("insert into icd10_codes (code, description, source_id) "
                + "values ('BAD-SOURCE', 'Synthetic mismatch', ?)", medicationSource))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void rejectsMissingQueryAndInvalidLimits() throws Exception {
        mvc.perform(get("/medical/catalogs/icd10")
                        .with(authentication(actor("PROFESSIONAL"))))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/medical/catalogs/medications").param("q", " ")
                        .with(authentication(actor("PROFESSIONAL"))))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/medical/catalogs/procedures").param("q", "test").param("limit", "51")
                        .with(authentication(actor("PROFESSIONAL"))))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/medical/catalogs/medications/{id}/presentations", UUID.randomUUID())
                        .param("limit", "0").with(authentication(actor("PROFESSIONAL"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void loadsSyntheticDemoPackIdempotentlyAndRejectsTampering() throws Exception {
        insertApprover();
        jdbc.update("insert into roles (name, description) values ('ADMIN', 'Synthetic administrator') "
                + "on conflict (name) do nothing");
        jdbc.update("insert into user_roles (user_id, role_id) "
                + "select ?, id from roles where name = 'ADMIN'", USER);
        Path script = Path.of("..", "..", "database", "demo",
                "load_synthetic_clinical_catalog.sql").toAbsolutePath().normalize();
        postgres.copyFileToContainer(MountableFile.forHostPath(script), "/tmp/demo-catalog.sql");

        assertThat(loadDemoCatalog("catalog-test-approver@example.test")).isZero();
        assertThat(loadDemoCatalog("catalog-test-approver@example.test")).isZero();
        assertThat(jdbc.queryForObject("select count(*) from medical_catalog_sources", Integer.class)).isEqualTo(3);
        assertThat(jdbc.queryForObject("select count(*) from icd10_codes", Integer.class)).isEqualTo(1);
        assertThat(jdbc.queryForObject("select count(*) from medications", Integer.class)).isEqualTo(2);
        assertThat(jdbc.queryForObject("select count(*) from medication_presentations", Integer.class)).isEqualTo(3);
        assertThat(jdbc.queryForObject("select count(*) from procedures", Integer.class)).isEqualTo(2);
        mvc.perform(get("/medical/catalogs/medications").param("q", "sintetico")
                        .with(authentication(actor("PROFESSIONAL"))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(2));
        mvc.perform(get("/medical/catalogs/procedures").param("q", "demo-proc")
                        .with(authentication(actor("PROFESSIONAL"))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(2));

        jdbc.update("update medications set generic_name = 'tampered' "
                + "where id = 'd2000000-0000-4000-8000-000000000001'");
        assertThat(loadDemoCatalog("catalog-test-approver@example.test")).isNotZero();
        assertThat(jdbc.queryForObject("select count(*) from medications", Integer.class)).isEqualTo(2);
        assertThat(loadDemoCatalog("missing@example.test")).isNotZero();
    }

    private long loadDemoCatalog(String approverEmail) throws Exception {
        return postgres.execInContainer("env", "PGPASSWORD=" + postgres.getPassword(),
                "psql", "-X", "-h", "localhost", "-U", postgres.getUsername(),
                "-d", postgres.getDatabaseName(), "-v", "approver_email=" + approverEmail,
                "-f", "/tmp/demo-catalog.sql").getExitCode();
    }

    private void insertApprover() {
        jdbc.update("insert into users (id, username, email, password_hash) "
                + "values (?, 'catalog-test-approver', 'catalog-test-approver@example.test', 'test')", USER);
    }

    private UUID source(String type) {
        UUID id = UUID.randomUUID();
        jdbc.update("insert into medical_catalog_sources "
                + "(id, catalog_type, source_name, source_version, license_reference, "
                + "approved_by_user_id, approved_at) "
                + "values (?, ?, 'Synthetic test fixture', 'test-v1', 'TEST-ONLY', ?, current_timestamp)",
                id, type, USER);
        return id;
    }

    private UsernamePasswordAuthenticationToken actor(String role) {
        AuthenticatedUser user = new AuthenticatedUser(USER, "catalog-test@example.test", "catalog-test",
                "test", true, Set.of(role), Set.of());
        return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    }
}
