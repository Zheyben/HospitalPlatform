package com.hospital.platform.medical;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.hospital.platform.users.service.AuthenticatedUser;
import com.hospital.platform.medical.service.MedicalFinalizationService;
import java.sql.Date;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
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
class MedicalContextIT {

    private static final ZoneId LIMA = ZoneId.of("America/Lima");
    private static final LocalDate APPOINTMENT_DATE = LocalDate.now(LIMA)
            .with(TemporalAdjusters.next(DayOfWeek.MONDAY));

    @TestConfiguration
    static class MedicalClockConfiguration {
        @Bean
        @Primary
        Clock medicalTestClock() {
            return Clock.fixed(APPOINTMENT_DATE.atTime(9, 0).atZone(LIMA).toInstant(), LIMA);
        }
    }

    private static final UUID DOCTOR_USER = UUID.fromString("10000000-0000-0000-0000-000000000001");
    private static final UUID OTHER_USER = UUID.fromString("10000000-0000-0000-0000-000000000002");
    private static final UUID PATIENT_USER = UUID.fromString("10000000-0000-0000-0000-000000000003");
    private static final UUID DOCTOR = UUID.fromString("20000000-0000-0000-0000-000000000001");
    private static final UUID OTHER_DOCTOR = UUID.fromString("20000000-0000-0000-0000-000000000002");
    private static final UUID PATIENT = UUID.fromString("30000000-0000-0000-0000-000000000001");
    private static final UUID SPECIALTY = UUID.fromString("40000000-0000-0000-0000-000000000001");
    private static final UUID SCHEDULE = UUID.fromString("50000000-0000-0000-0000-000000000001");
    private static final UUID OTHER_SCHEDULE = UUID.fromString("50000000-0000-0000-0000-000000000002");
    private static final UUID TODAY_SLOT = UUID.fromString("60000000-0000-0000-0000-000000000001");
    private static final UUID TOMORROW_SLOT = UUID.fromString("60000000-0000-0000-0000-000000000002");
    private static final UUID OTHER_SLOT = UUID.fromString("60000000-0000-0000-0000-000000000003");
    private UUID todayAppointment;
    private UUID tomorrowAppointment;
    private UUID otherAppointment;

    @Container
    static final PostgreSQLContainer postgres = new PostgreSQLContainer(
            DockerImageName.parse("postgres:16-alpine"))
            .withDatabaseName("hospital_platform_medical_context_test")
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
    @Autowired private MedicalFinalizationService finalization;

    @BeforeEach
    void setUp() {
        jdbc.execute("truncate table audit_logs, appointments, availability_slots, schedules, "
                + "professional_specialties, patients, professionals, specialties, refresh_tokens, "
                + "user_roles, users cascade");
        jdbc.update("insert into users (id, username, email, password_hash, first_name, last_name) values "
                + "(?, 'med-context-doctor', 'med-context-doctor@example.test', 'test', 'Ana', 'Rojas'), "
                + "(?, 'med-context-other', 'med-context-other@example.test', 'test', 'Luis', 'Perez'), "
                + "(?, 'med-context-patient', 'med-context-patient@example.test', 'test', 'Elena', 'Diaz')",
                DOCTOR_USER, OTHER_USER, PATIENT_USER);
        jdbc.update("insert into patients (id, user_id, document_type, document_number, insurance) "
                + "values (?, ?, 'DNI', '95000001', 'SIS')", PATIENT, PATIENT_USER);
        jdbc.update("insert into specialties (id, name) values (?, 'Medicina General')", SPECIALTY);
        jdbc.update("insert into professionals (id, user_id, license_number) values "
                + "(?, ?, '123451'), (?, ?, '123452')",
                DOCTOR, DOCTOR_USER, OTHER_DOCTOR, OTHER_USER);
        jdbc.update("insert into professional_specialties (professional_id, specialty_id) values (?, ?), (?, ?)",
                DOCTOR, SPECIALTY, OTHER_DOCTOR, SPECIALTY);
        jdbc.update("insert into schedules "
                + "(id, professional_id, specialty_id, day_of_week, start_time, end_time) "
                + "values (?, ?, ?, 1, time '08:00', time '12:00'), "
                + "(?, ?, ?, 1, time '08:00', time '12:00')",
                SCHEDULE, DOCTOR, SPECIALTY, OTHER_SCHEDULE, OTHER_DOCTOR, SPECIALTY);
        insertSlot(TODAY_SLOT, SCHEDULE, APPOINTMENT_DATE, "08:00");
        insertSlot(TOMORROW_SLOT, SCHEDULE, APPOINTMENT_DATE.plusWeeks(1), "09:00");
        insertSlot(OTHER_SLOT, OTHER_SCHEDULE, APPOINTMENT_DATE, "10:00");
        todayAppointment = insertWaitingAppointment(TODAY_SLOT);
        tomorrowAppointment = insertWaitingAppointment(TOMORROW_SLOT);
        otherAppointment = insertWaitingAppointment(OTHER_SLOT);
    }

    @Test
    void showsOnlyOwnTodaysQueueWithoutReason() throws Exception {
        mvc.perform(get("/medical/me/context").with(authentication(actor(DOCTOR_USER, "PROFESSIONAL"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.professionalId").value(DOCTOR.toString()))
                .andExpect(jsonPath("$.simulatedRne").value("SIM-RNE-000000000001"))
                .andExpect(jsonPath("$.specialtyName").value("Medicina General"))
                .andExpect(jsonPath("$.readyAppointments.length()").value(1))
                .andExpect(jsonPath("$.readyAppointments[0].appointmentId").value(todayAppointment.toString()))
                .andExpect(jsonPath("$.readyAppointments[0].reason").doesNotExist());
    }

    @Test
    void preservesSimulatedProfessionalAndEncounterFieldsAcrossReadsAndRetries() throws Exception {
        jdbc.update("update professionals set license_number = '654321' where id = ?", DOCTOR);
        mvc.perform(get("/medical/me/context")
                        .with(authentication(actor(DOCTOR_USER, "PROFESSIONAL"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.licenseNumber").value("654321"))
                .andExpect(jsonPath("$.simulatedRne").value("SIM-RNE-000000000001"));

        mvc.perform(post("/medical/appointments/{id}/start", todayAppointment)
                        .with(authentication(actor(DOCTOR_USER, "PROFESSIONAL"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.simulatedRne").value("SIM-RNE-000000000001"))
                .andExpect(jsonPath("$.simulatedCareType").value("Consulta externa (dato simulado)"))
                .andExpect(jsonPath("$.simulatedService").value("Servicio ambulatorio (dato simulado)"));
        mvc.perform(post("/medical/appointments/{id}/start", todayAppointment)
                        .with(authentication(actor(DOCTOR_USER, "PROFESSIONAL"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.simulatedRne").value("SIM-RNE-000000000001"))
                .andExpect(jsonPath("$.simulatedCareType").value("Consulta externa (dato simulado)"));
        assertThat(jdbc.queryForObject("select count(*) from clinical_encounters where appointment_id = ?",
                Integer.class, todayAppointment)).isEqualTo(1);
        assertThat(jdbc.queryForObject("select simulated_service from clinical_encounters where appointment_id = ?",
                String.class, todayAppointment)).isEqualTo("Servicio ambulatorio (dato simulado)");

        jdbc.update("insert into clinical_encounters (appointment_id, legacy_start) values (?, true)",
                otherAppointment);
        mvc.perform(post("/medical/appointments/{id}/start", otherAppointment)
                        .with(authentication(actor(OTHER_USER, "PROFESSIONAL"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.legacyStart").value(true))
                .andExpect(jsonPath("$.startedAt").isEmpty())
                .andExpect(jsonPath("$.simulatedRne").value("SIM-RNE-000000000002"))
                .andExpect(jsonPath("$.simulatedCareType").value("Consulta externa (dato simulado)"));
        assertThat(jdbc.queryForObject("select started_at from clinical_encounters where appointment_id = ?",
                java.time.OffsetDateTime.class, otherAppointment)).isNull();
    }

    @Test
    void permitsAssignedContextAndHidesUnassignedOrNonReadyAppointments() throws Exception {
        jdbc.update("update patients set sex = 'Femenino', district = 'Lima', "
                + "emergency_contact_name = 'Persona Sintetica' where id = ?", PATIENT);
        mvc.perform(get("/medical/appointments/{id}/context", todayAppointment)
                        .with(authentication(actor(DOCTOR_USER, "PROFESSIONAL"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.documentNumber").value("95000001"))
                .andExpect(jsonPath("$.sex").value("Femenino"))
                .andExpect(jsonPath("$.district").value("Lima"))
                .andExpect(jsonPath("$.emergencyContactName").value("Persona Sintetica"))
                .andExpect(jsonPath("$.clinicalRecordNumber").isNotEmpty())
                .andExpect(jsonPath("$.reason").value("Clinical context fixture"));
        mvc.perform(get("/medical/appointments/{id}/history", todayAppointment)
                        .with(authentication(actor(DOCTOR_USER, "PROFESSIONAL"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(0))
                .andExpect(jsonPath("$.hasMore").value(false));
        mvc.perform(get("/medical/appointments/{id}/history", otherAppointment)
                        .with(authentication(actor(DOCTOR_USER, "PROFESSIONAL"))))
                .andExpect(status().isNotFound());
        mvc.perform(get("/medical/appointments/{id}/history", todayAppointment)
                        .with(authentication(actor(OTHER_USER, "RECEPTIONIST"))))
                .andExpect(status().isForbidden());
        mvc.perform(get("/medical/appointments/{id}/history?limit=0", todayAppointment)
                        .with(authentication(actor(DOCTOR_USER, "PROFESSIONAL"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_MEDICAL_HISTORY_PAGE"));
        mvc.perform(get("/medical/appointments/{id}/context", otherAppointment)
                        .with(authentication(actor(DOCTOR_USER, "PROFESSIONAL"))))
                .andExpect(status().isNotFound());
        mvc.perform(get("/medical/appointments/{id}/context", tomorrowAppointment)
                        .with(authentication(actor(DOCTOR_USER, "PROFESSIONAL"))))
                .andExpect(status().isNotFound());
        UUID scheduledSlot = UUID.fromString("60000000-0000-0000-0000-000000000004");
        insertSlot(scheduledSlot, SCHEDULE, APPOINTMENT_DATE.plusWeeks(2), "11:00");
        UUID scheduledAppointment = jdbc.queryForObject("select capacity_reserve(?,?,?)", UUID.class,
                scheduledSlot, PATIENT, "Unconfirmed");
        mvc.perform(get("/medical/appointments/{id}/context", scheduledAppointment)
                        .with(authentication(actor(DOCTOR_USER, "PROFESSIONAL"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void rejectsOtherRolesAndInactiveProfessional() throws Exception {
        mvc.perform(get("/medical/me/context").with(authentication(actor(PATIENT_USER, "PATIENT"))))
                .andExpect(status().isForbidden());
        mvc.perform(get("/medical/appointments/{id}/context", todayAppointment)
                        .with(authentication(actor(OTHER_USER, "RECEPTIONIST"))))
                .andExpect(status().isForbidden());
        mvc.perform(get("/medical/me/context")).andExpect(status().isUnauthorized());
        mvc.perform(get("/medical/me/context").with(authentication(actor(PATIENT_USER, "PROFESSIONAL"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void savesAndReloadsVersionedHistoryWithoutPublishingClinicalData() throws Exception {
        UUID encounterId = startTodayAppointment();
        String first = """
                {"version":0,"history":{"personal":{"allergiesAndReactions":"Synthetic allergy note"},
                "family":{"father":"Synthetic family note"},"gynecologic":{"pregnancies":0}}}
                """;

        mvc.perform(get("/medical/encounters/{id}/draft/history", encounterId)
                        .with(authentication(actor(DOCTOR_USER, "PROFESSIONAL"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.version").value(0))
                .andExpect(jsonPath("$.history").isEmpty());
        mvc.perform(put("/medical/encounters/{id}/draft/history", encounterId)
                        .with(authentication(actor(DOCTOR_USER, "PROFESSIONAL")))
                        .contentType(MediaType.APPLICATION_JSON).content(first))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.version").value(1))
                .andExpect(jsonPath("$.history.personal.allergiesAndReactions").value("Synthetic allergy note"))
                .andExpect(jsonPath("$.history.gynecologic.pregnancies").value(0));
        mvc.perform(get("/medical/encounters/{id}/draft/history", encounterId)
                        .with(authentication(actor(DOCTOR_USER, "PROFESSIONAL"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.version").value(1))
                .andExpect(jsonPath("$.history.family.father").value("Synthetic family note"));

        String second = """
                {"version":1,"history":{"personal":{"allergiesAndReactions":"Changed synthetic note"}}}
                """;
        mvc.perform(put("/medical/encounters/{id}/draft/history", encounterId)
                        .with(authentication(actor(DOCTOR_USER, "PROFESSIONAL")))
                        .contentType(MediaType.APPLICATION_JSON).content(second))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.version").value(2));
        mvc.perform(put("/medical/encounters/{id}/draft/history", encounterId)
                        .with(authentication(actor(DOCTOR_USER, "PROFESSIONAL")))
                        .contentType(MediaType.APPLICATION_JSON).content(second))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("MEDICAL_DRAFT_VERSION_CONFLICT"));

        assertThat(jdbc.queryForObject("select version from encounter_drafts where encounter_id = ?",
                Integer.class, encounterId)).isEqualTo(2);
        assertThat(jdbc.queryForObject("select count(*) from audit_logs where action = 'CLINICAL_DRAFT_SAVED'",
                Integer.class)).isEqualTo(2);
        assertThat(jdbc.queryForObject("select string_agg(new_values::text, '') from audit_logs "
                + "where action = 'CLINICAL_DRAFT_SAVED'", String.class))
                .doesNotContain("Synthetic allergy note", "Synthetic family note");
    }

    @Test
    void hidesHistoryDraftFromOtherDoctorsAndNonMedicalRoles() throws Exception {
        UUID encounterId = startTodayAppointment();
        mvc.perform(get("/medical/encounters/{id}/draft/history", encounterId)
                        .with(authentication(actor(OTHER_USER, "PROFESSIONAL"))))
                .andExpect(status().isNotFound());
        mvc.perform(put("/medical/encounters/{id}/draft/history", encounterId)
                        .with(authentication(actor(OTHER_USER, "PROFESSIONAL")))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"version\":0,\"history\":{}}"))
                .andExpect(status().isNotFound());
        mvc.perform(get("/medical/encounters/{id}/draft/history", encounterId)
                        .with(authentication(actor(PATIENT_USER, "PATIENT"))))
                .andExpect(status().isForbidden());
        mvc.perform(get("/medical/encounters/{id}/draft/history", encounterId)
                        .with(authentication(actor(OTHER_USER, "RECEPTIONIST"))))
                .andExpect(status().isForbidden());
        mvc.perform(get("/medical/encounters/{id}/draft/history", encounterId)
                        .with(authentication(actor(OTHER_USER, "ADMIN"))))
                .andExpect(status().isForbidden());
        mvc.perform(get("/medical/encounters/{id}/draft/history", encounterId))
                .andExpect(status().isUnauthorized());
        assertThat(jdbc.queryForObject("select count(*) from encounter_drafts", Integer.class)).isZero();
    }

    @Test
    void validatesHistoryAndRejectsEditsAfterEncounterIsFinalized() throws Exception {
        UUID encounterId = startTodayAppointment();
        mvc.perform(put("/medical/encounters/{id}/draft/history", encounterId)
                        .with(authentication(actor(DOCTOR_USER, "PROFESSIONAL")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"version\":0,\"history\":{\"gynecologic\":{\"pregnancies\":-1}}}"))
                .andExpect(status().isBadRequest());
        mvc.perform(put("/medical/encounters/{id}/draft/history", encounterId)
                        .with(authentication(actor(DOCTOR_USER, "PROFESSIONAL")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"version\":0,\"history\":{\"personal\":{\"unknown\":\"ignored?\"}}}"))
                .andExpect(status().isBadRequest());
        assertThat(jdbc.queryForObject("select count(*) from encounter_drafts", Integer.class)).isZero();

        jdbc.update("update clinical_encounters set status = 'FINALIZED' where id = ?", encounterId);
        mvc.perform(get("/medical/encounters/{id}/draft/history", encounterId)
                        .with(authentication(actor(DOCTOR_USER, "PROFESSIONAL"))))
                .andExpect(status().isNotFound());
        mvc.perform(put("/medical/encounters/{id}/draft/history", encounterId)
                        .with(authentication(actor(DOCTOR_USER, "PROFESSIONAL")))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"version\":0,\"history\":{}}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void keepsAssessmentAndHistorySeparateWithOneOptimisticVersion() throws Exception {
        UUID encounterId = startTodayAppointment();
        mvc.perform(put("/medical/encounters/{id}/draft/history", encounterId)
                        .with(authentication(actor(DOCTOR_USER, "PROFESSIONAL")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"version\":0,\"history\":{\"otherAlerts\":\"Synthetic history alert\"}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.version").value(1));

        mvc.perform(get("/medical/encounters/{id}/draft/assessment", encounterId)
                        .with(authentication(actor(DOCTOR_USER, "PROFESSIONAL"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.version").value(1))
                .andExpect(jsonPath("$.assessment").isEmpty());
        mvc.perform(put("/medical/encounters/{id}/draft/assessment", encounterId)
                        .with(authentication(actor(DOCTOR_USER, "PROFESSIONAL")))
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"version":1,"assessment":{
                                  "presentation":{"reason":"Synthetic reason","symptomsAndCurrentIllness":"Synthetic symptoms",
                                                  "illnessDuration":2,"illnessDurationUnit":"DAYS"},
                                  "vitalSigns":{"heartRate":72,"oxygenSaturationPercent":98},
                                  "diagnosis":{"primaryDiagnosis":"Synthetic working diagnosis",
                                               "diagnosisType":"PRESUMPTIVE"},
                                  "treatmentPlan":{"generalIndications":"Synthetic draft indications"}
                                }}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.version").value(2))
                .andExpect(jsonPath("$.assessment.vitalSigns.heartRate").value(72));
        mvc.perform(get("/medical/encounters/{id}/draft/history", encounterId)
                        .with(authentication(actor(DOCTOR_USER, "PROFESSIONAL"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.version").value(2))
                .andExpect(jsonPath("$.history.otherAlerts").value("Synthetic history alert"));
        mvc.perform(put("/medical/encounters/{id}/draft/history", encounterId)
                        .with(authentication(actor(DOCTOR_USER, "PROFESSIONAL")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"version\":1,\"history\":{\"otherAlerts\":\"Stale value\"}}"))
                .andExpect(status().isConflict());
        assertThat(jdbc.queryForObject("select jsonb_exists(content, 'history') "
                + "and jsonb_exists(content, 'assessment') "
                + "from encounter_drafts where encounter_id = ?", Boolean.class, encounterId)).isTrue();
        assertThat(jdbc.queryForObject("select string_agg(new_values::text, '') from audit_logs "
                + "where action = 'CLINICAL_DRAFT_SAVED'", String.class))
                .doesNotContain("Synthetic reason", "Synthetic working diagnosis", "Synthetic history alert");
    }

    @Test
    void validatesAssessmentNumbersSelectionsAndActiveCatalogReferences() throws Exception {
        UUID encounterId = startTodayAppointment();
        UUID unknown = UUID.randomUUID();
        mvc.perform(put("/medical/encounters/{id}/draft/assessment", encounterId)
                        .with(authentication(actor(DOCTOR_USER, "PROFESSIONAL")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"version\":0,\"assessment\":{\"vitalSigns\":{\"oxygenSaturationPercent\":0}}}"))
                .andExpect(status().isBadRequest());
        mvc.perform(put("/medical/encounters/{id}/draft/assessment", encounterId)
                        .with(authentication(actor(DOCTOR_USER, "PROFESSIONAL")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"version\":0,\"assessment\":{\"presentation\":{\"illnessDuration\":-1}}}"))
                .andExpect(status().isBadRequest());
        mvc.perform(put("/medical/encounters/{id}/draft/assessment", encounterId)
                        .with(authentication(actor(DOCTOR_USER, "PROFESSIONAL")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"version\":0,\"assessment\":{\"diagnosis\":{\"diagnosisType\":\"UNKNOWN\"}}}"))
                .andExpect(status().isBadRequest());
        mvc.perform(put("/medical/encounters/{id}/draft/assessment", encounterId)
                        .with(authentication(actor(DOCTOR_USER, "PROFESSIONAL")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"version\":0,\"assessment\":{\"diagnosis\":{\"unknown\":\"ignored?\"}}}"))
                .andExpect(status().isBadRequest());
        mvc.perform(put("/medical/encounters/{id}/draft/assessment", encounterId)
                        .with(authentication(actor(DOCTOR_USER, "PROFESSIONAL")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"version\":0,\"assessment\":{\"diagnosis\":{\"icd10CodeId\":\"" + unknown
                                + "\"}}}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_MEDICAL_DRAFT_REFERENCE"));
        mvc.perform(put("/medical/encounters/{id}/draft/assessment", encounterId)
                        .with(authentication(actor(DOCTOR_USER, "PROFESSIONAL")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"version\":0,\"assessment\":{\"diagnosis\":{\"procedureId\":\"" + unknown
                                + "\"}}}"))
                .andExpect(status().isBadRequest());
        mvc.perform(put("/medical/encounters/{id}/draft/assessment", encounterId)
                        .with(authentication(actor(DOCTOR_USER, "PROFESSIONAL")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"version\":0,\"assessment\":{\"treatmentPlan\":{\"referralType\":\"NONE\","
                                + "\"referralSpecialtyId\":\"" + SPECIALTY + "\"}}}"))
                .andExpect(status().isBadRequest());
        assertThat(jdbc.queryForObject("select count(*) from encounter_drafts", Integer.class)).isZero();

        UUID icdSource = UUID.randomUUID();
        UUID procedureSource = UUID.randomUUID();
        UUID icdId = UUID.randomUUID();
        UUID procedureId = UUID.randomUUID();
        jdbc.update("insert into medical_catalog_sources "
                        + "(id, catalog_type, source_name, source_version, license_reference, approved_by_user_id, approved_at) "
                        + "values (?, 'ICD10', 'Synthetic test source', '1', 'TEST ONLY', ?, current_timestamp), "
                        + "(?, 'PROCEDURE', 'Synthetic test source', '1', 'TEST ONLY', ?, current_timestamp)",
                icdSource, DOCTOR_USER, procedureSource, DOCTOR_USER);
        jdbc.update("insert into icd10_codes (id, code, description, source_id) values "
                + "(?, 'TEST-ICD', 'Synthetic code', ?)", icdId, icdSource);
        jdbc.update("insert into procedures (id, code, name, source_id) values "
                + "(?, 'TEST-PROC', 'Synthetic procedure', ?)", procedureId, procedureSource);
        mvc.perform(put("/medical/encounters/{id}/draft/assessment", encounterId)
                        .with(authentication(actor(DOCTOR_USER, "PROFESSIONAL")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"version\":0,\"assessment\":{\"diagnosis\":{\"icd10CodeId\":\"" + icdId
                                + "\",\"procedureId\":\"" + procedureId + "\"},\"treatmentPlan\":{"
                                + "\"referralType\":\"INTERCONSULTATION\",\"referralSpecialtyId\":\"" + SPECIALTY
                                + "\"}}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.version").value(1))
                .andExpect(jsonPath("$.assessment.diagnosis.icd10CodeId").value(icdId.toString()));

        jdbc.update("update icd10_codes set active = false where id = ?", icdId);
        mvc.perform(put("/medical/encounters/{id}/draft/assessment", encounterId)
                        .with(authentication(actor(DOCTOR_USER, "PROFESSIONAL")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"version\":1,\"assessment\":{\"diagnosis\":{\"icd10CodeId\":\"" + icdId
                                + "\"}}}"))
                .andExpect(status().isBadRequest());
        assertThat(jdbc.queryForObject("select version from encounter_drafts where encounter_id = ?",
                Integer.class, encounterId)).isEqualTo(1);
    }

    @Test
    void hidesAssessmentDraftFromOtherDoctorsAndRoles() throws Exception {
        UUID encounterId = startTodayAppointment();
        mvc.perform(get("/medical/encounters/{id}/draft/assessment", encounterId)
                        .with(authentication(actor(OTHER_USER, "PROFESSIONAL"))))
                .andExpect(status().isNotFound());
        mvc.perform(put("/medical/encounters/{id}/draft/assessment", encounterId)
                        .with(authentication(actor(OTHER_USER, "PROFESSIONAL")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"version\":0,\"assessment\":{\"diagnosis\":{\"icd10CodeId\":\""
                                + UUID.randomUUID() + "\"}}}"))
                .andExpect(status().isNotFound());
        mvc.perform(get("/medical/encounters/{id}/draft/assessment", encounterId)
                        .with(authentication(actor(PATIENT_USER, "PATIENT"))))
                .andExpect(status().isForbidden());
        mvc.perform(get("/medical/encounters/{id}/draft/assessment", encounterId)
                        .with(authentication(actor(OTHER_USER, "RECEPTIONIST"))))
                .andExpect(status().isForbidden());
        mvc.perform(get("/medical/encounters/{id}/draft/assessment", encounterId)
                        .with(authentication(actor(OTHER_USER, "ADMIN"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void preservesOtherDraftSectionsWhenSavingPrescriptionWithoutMedicationItems() throws Exception {
        UUID encounterId = startTodayAppointment();
        mvc.perform(put("/medical/encounters/{id}/draft/history", encounterId)
                        .with(authentication(actor(DOCTOR_USER, "PROFESSIONAL")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"version\":0,\"history\":{\"otherAlerts\":\"Synthetic allergy\"}}"))
                .andExpect(status().isOk());
        mvc.perform(put("/medical/encounters/{id}/draft/assessment", encounterId)
                        .with(authentication(actor(DOCTOR_USER, "PROFESSIONAL")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"version\":1,\"assessment\":{\"diagnosis\":{\"primaryDiagnosis\":\"Synthetic diagnosis\"}}}"))
                .andExpect(status().isOk());

        mvc.perform(get("/medical/encounters/{id}/draft/prescription", encounterId)
                        .with(authentication(actor(DOCTOR_USER, "PROFESSIONAL"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.version").value(2))
                .andExpect(jsonPath("$.prescription").isEmpty());
        mvc.perform(put("/medical/encounters/{id}/draft/prescription", encounterId)
                        .with(authentication(actor(DOCTOR_USER, "PROFESSIONAL")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"version\":2,\"prescription\":{\"additionalPrecautions\":\"Synthetic precaution\",\"items\":[]}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.version").value(3))
                .andExpect(jsonPath("$.prescription.items.length()").value(0));
        mvc.perform(get("/medical/encounters/{id}/draft/history", encounterId)
                        .with(authentication(actor(DOCTOR_USER, "PROFESSIONAL"))))
                .andExpect(jsonPath("$.version").value(3))
                .andExpect(jsonPath("$.history.otherAlerts").value("Synthetic allergy"));
        mvc.perform(get("/medical/encounters/{id}/draft/assessment", encounterId)
                        .with(authentication(actor(DOCTOR_USER, "PROFESSIONAL"))))
                .andExpect(jsonPath("$.assessment.diagnosis.primaryDiagnosis").value("Synthetic diagnosis"));
        mvc.perform(put("/medical/encounters/{id}/draft/prescription", encounterId)
                        .with(authentication(actor(DOCTOR_USER, "PROFESSIONAL")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"version\":2,\"prescription\":{\"items\":[]}}"))
                .andExpect(status().isConflict());
        assertThat(jdbc.queryForObject("select jsonb_exists(content, 'prescription') "
                + "and jsonb_exists(content, 'assessment') and jsonb_exists(content, 'history') "
                + "from encounter_drafts where encounter_id = ?", Boolean.class, encounterId)).isTrue();
        assertThat(jdbc.queryForObject("select string_agg(new_values::text, '') from audit_logs "
                + "where action = 'CLINICAL_DRAFT_SAVED'", String.class))
                .doesNotContain("Synthetic precaution", "Synthetic allergy", "Synthetic diagnosis");
    }

    @Test
    void requiresCompletePrescriptionRowsAndRelatedActiveCatalogSelections() throws Exception {
        UUID encounterId = startTodayAppointment();
        UUID medicationId = UUID.randomUUID();
        UUID otherMedicationId = UUID.randomUUID();
        UUID presentationId = UUID.randomUUID();
        String item = """
                {"medicationId":"%s","presentationId":"%s","dose":1.5,"doseUnit":"TABLET",
                 "frequency":"EVERY_8_HOURS","route":"ORAL","duration":5,"durationUnit":"DAYS",
                 "quantity":15,"usageInstructions":"Synthetic use only"}
                """.formatted(medicationId, presentationId);
        mvc.perform(put("/medical/encounters/{id}/draft/prescription", encounterId)
                        .with(authentication(actor(DOCTOR_USER, "PROFESSIONAL")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"version\":0,\"prescription\":{\"items\":[{\"medicationId\":\"" + medicationId
                                + "\"}]}}"))
                .andExpect(status().isBadRequest());
        mvc.perform(put("/medical/encounters/{id}/draft/prescription", encounterId)
                        .with(authentication(actor(DOCTOR_USER, "PROFESSIONAL")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"version\":0,\"prescription\":{\"items\":["
                                + item.replace("\"dose\":1.5", "\"dose\":0") + "]}}"))
                .andExpect(status().isBadRequest());
        mvc.perform(put("/medical/encounters/{id}/draft/prescription", encounterId)
                        .with(authentication(actor(DOCTOR_USER, "PROFESSIONAL")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"version\":0,\"prescription\":{\"items\":["
                                + item.replace("EVERY_8_HOURS", "EVERY_HOUR") + "]}}"))
                .andExpect(status().isBadRequest());
        mvc.perform(put("/medical/encounters/{id}/draft/prescription", encounterId)
                        .with(authentication(actor(DOCTOR_USER, "PROFESSIONAL")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"version\":0,\"prescription\":{\"items\":[]},\"unknown\":true}"))
                .andExpect(status().isBadRequest());
        mvc.perform(put("/medical/encounters/{id}/draft/prescription", encounterId)
                        .with(authentication(actor(DOCTOR_USER, "PROFESSIONAL")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"version\":0,\"prescription\":{\"items\":[" + item + "]}}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_MEDICAL_DRAFT_REFERENCE"));

        UUID sourceId = UUID.randomUUID();
        jdbc.update("insert into medical_catalog_sources "
                        + "(id, catalog_type, source_name, source_version, license_reference, approved_by_user_id, approved_at) "
                        + "values (?, 'MEDICATION', 'Synthetic prescription test source', '1', 'TEST ONLY', ?, current_timestamp)",
                sourceId, DOCTOR_USER);
        jdbc.update("insert into medications (id, generic_name, source_id) values "
                + "(?, 'Synthetic medicine', ?), (?, 'Other synthetic medicine', ?)",
                medicationId, sourceId, otherMedicationId, sourceId);
        jdbc.update("insert into medication_presentations "
                        + "(id, medication_id, name, concentration, pharmaceutical_form) "
                        + "values (?, ?, 'Synthetic tablets', '10 mg', 'TABLET')",
                presentationId, medicationId);
        mvc.perform(put("/medical/encounters/{id}/draft/prescription", encounterId)
                        .with(authentication(actor(DOCTOR_USER, "PROFESSIONAL")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"version\":0,\"prescription\":{\"items\":["
                                + item.replace(medicationId.toString(), otherMedicationId.toString()) + "]}}"))
                .andExpect(status().isBadRequest());
        jdbc.update("update medications set active = false where id = ?", medicationId);
        mvc.perform(put("/medical/encounters/{id}/draft/prescription", encounterId)
                        .with(authentication(actor(DOCTOR_USER, "PROFESSIONAL")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"version\":0,\"prescription\":{\"items\":[" + item + "]}}"))
                .andExpect(status().isBadRequest());
        jdbc.update("update medications set active = true where id = ?", medicationId);
        mvc.perform(put("/medical/encounters/{id}/draft/prescription", encounterId)
                        .with(authentication(actor(DOCTOR_USER, "PROFESSIONAL")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"version\":0,\"prescription\":{\"items\":[" + item + "]}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.version").value(1))
                .andExpect(jsonPath("$.prescription.items[0].presentationId").value(presentationId.toString()));
        jdbc.update("update medication_presentations set active = false where id = ?", presentationId);
        mvc.perform(put("/medical/encounters/{id}/draft/prescription", encounterId)
                        .with(authentication(actor(DOCTOR_USER, "PROFESSIONAL")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"version\":1,\"prescription\":{\"items\":[" + item + "]}}"))
                .andExpect(status().isBadRequest());
        assertThat(jdbc.queryForObject("select version from encounter_drafts where encounter_id = ?",
                Integer.class, encounterId)).isEqualTo(1);
    }

    @Test
    void hidesPrescriptionDraftFromOtherDoctorsAndNonMedicalRoles() throws Exception {
        UUID encounterId = startTodayAppointment();
        mvc.perform(get("/medical/encounters/{id}/draft/prescription", encounterId)
                        .with(authentication(actor(OTHER_USER, "PROFESSIONAL"))))
                .andExpect(status().isNotFound());
        mvc.perform(put("/medical/encounters/{id}/draft/prescription", encounterId)
                        .with(authentication(actor(OTHER_USER, "PROFESSIONAL")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"version\":0,\"prescription\":{\"items\":[{\"medicationId\":\""
                                + UUID.randomUUID() + "\",\"presentationId\":\"" + UUID.randomUUID()
                                + "\",\"dose\":1,\"doseUnit\":\"MG\",\"frequency\":\"ONCE\",\"route\":\"ORAL\","
                                + "\"duration\":1,\"durationUnit\":\"DAYS\",\"quantity\":1,"
                                + "\"usageInstructions\":\"Synthetic use\"}]}}"))
                .andExpect(status().isNotFound());
        mvc.perform(get("/medical/encounters/{id}/draft/prescription", encounterId)
                        .with(authentication(actor(PATIENT_USER, "PATIENT"))))
                .andExpect(status().isForbidden());
        mvc.perform(get("/medical/encounters/{id}/draft/prescription", encounterId)
                        .with(authentication(actor(OTHER_USER, "RECEPTIONIST"))))
                .andExpect(status().isForbidden());
        mvc.perform(get("/medical/encounters/{id}/draft/prescription", encounterId)
                        .with(authentication(actor(OTHER_USER, "ADMIN"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void finalRecordSchemaKeepsSnapshotsAndRejectsUnrelatedPresentation() throws Exception {
        UUID encounterId = startTodayAppointment();
        assertThat(jdbc.queryForObject("""
                select to_regclass('clinical_history_entries') is not null
                   and to_regclass('clinical_assessments') is not null
                   and to_regclass('clinical_diagnoses') is not null
                   and to_regclass('clinical_treatment_plans') is not null
                   and to_regclass('clinical_orders') is not null
                """, Boolean.class)).isTrue();
        assertThat(jdbc.queryForObject("select count(*) from clinical_final_records", Integer.class)).isZero();

        jdbc.update("""
                insert into clinical_final_records
                    (encounter_id, patient_id, professional_id, clinical_record_number,
                     patient_document_type, patient_document_number, professional_license_number,
                     professional_simulated_rne, specialty_name, appointment_date,
                     appointment_start_time, finalized_at, draft_version, content_sha256)
                select ?, ?, ?, cr.record_number, 'DNI', '95000001', '123451', p.simulated_rne,
                       'Medicina General', ?, time '08:00', current_timestamp, 0, ?
                from clinical_records cr cross join professionals p
                where cr.patient_id = ? and p.id = ?
                """, encounterId, PATIENT, DOCTOR, Date.valueOf(APPOINTMENT_DATE), "a".repeat(64),
                PATIENT, DOCTOR);
        assertThat(jdbc.queryForObject(
                "select patient_sex from clinical_final_records where encounter_id = ?",
                String.class, encounterId)).isNull();
        UUID sourceId = UUID.randomUUID();
        UUID medicationId = UUID.randomUUID();
        UUID otherMedicationId = UUID.randomUUID();
        UUID presentationId = UUID.randomUUID();
        jdbc.update("""
                insert into medical_catalog_sources
                    (id, catalog_type, source_name, source_version, license_reference,
                     approved_by_user_id, approved_at)
                values (?, 'MEDICATION', 'Synthetic final schema source', '1', 'TEST ONLY', ?, current_timestamp)
                """, sourceId, DOCTOR_USER);
        jdbc.update("insert into medications (id, generic_name, source_id) values "
                + "(?, 'Original generic', ?), (?, 'Other generic', ?)",
                medicationId, sourceId, otherMedicationId, sourceId);
        jdbc.update("""
                insert into medication_presentations
                    (id, medication_id, name, concentration, pharmaceutical_form)
                values (?, ?, 'Tablets', '10 mg', 'TABLET')
                """, presentationId, medicationId);
        UUID prescriptionId = jdbc.queryForObject("""
                insert into clinical_prescriptions (encounter_id, issued_at)
                values (?, current_timestamp) returning id
                """, UUID.class, encounterId);
        String itemInsert = """
                insert into clinical_prescription_items
                    (prescription_id, item_number, medication_id, presentation_id,
                     medication_name_snapshot, presentation_name_snapshot, concentration_snapshot,
                     pharmaceutical_form_snapshot, dose, dose_unit, frequency, route,
                     duration, duration_unit, quantity, usage_instructions)
                values (?, 1, ?, ?, 'Original generic', 'Tablets', '10 mg', 'TABLET',
                        1, 'MG', 'ONCE', 'ORAL', 1, 'DAYS', 1, 'Synthetic instruction')
                """;
        assertThatThrownBy(() -> jdbc.update(itemInsert, prescriptionId, otherMedicationId, presentationId))
                .isInstanceOf(DataIntegrityViolationException.class);
        jdbc.update(itemInsert, prescriptionId, medicationId, presentationId);
        jdbc.update("update medications set generic_name = 'Changed catalog name' where id = ?", medicationId);
        assertThat(jdbc.queryForObject("""
                select medication_name_snapshot from clinical_prescription_items
                where prescription_id = ? and item_number = 1
                """, String.class, prescriptionId)).isEqualTo("Original generic");
        jdbc.update("update clinical_encounters set status = 'FINALIZED' where id = ?", encounterId);
        setSyntheticBusinessNow();
        try {
            assertThatThrownBy(() -> jdbc.queryForObject(
                    "select capacity_appointment_complete(?)", Object.class, todayAppointment))
                    .isInstanceOf(DataIntegrityViolationException.class);
        } finally {
            restoreBusinessNow();
        }
    }

    @Test
    void finalizesOneClinicalRecordAndPrescriptionIdempotently() throws Exception {
        UUID encounterId = startTodayAppointment();
        jdbc.update("update patients set sex = 'Femenino', district = 'Lima' where id = ?", PATIENT);
        UUID icdSource = UUID.randomUUID();
        UUID medicationSource = UUID.randomUUID();
        UUID codeId = UUID.randomUUID();
        UUID medicationId = UUID.randomUUID();
        UUID presentationId = UUID.randomUUID();
        jdbc.update("""
                insert into medical_catalog_sources
                    (id, catalog_type, source_name, source_version, license_reference,
                     approved_by_user_id, approved_at)
                values (?, 'ICD10', 'Synthetic ICD', '1', 'TEST ONLY', ?, current_timestamp),
                       (?, 'MEDICATION', 'Synthetic drug', '1', 'TEST ONLY', ?, current_timestamp)
                """, icdSource, DOCTOR_USER, medicationSource, DOCTOR_USER);
        jdbc.update("insert into icd10_codes (id, code, description, source_id) "
                + "values (?, 'SYN-001', 'Synthetic diagnosis', ?)", codeId, icdSource);
        jdbc.update("insert into medications (id, generic_name, source_id) "
                + "values (?, 'Synthetic medicine', ?)", medicationId, medicationSource);
        jdbc.update("""
                insert into medication_presentations
                    (id, medication_id, name, concentration, pharmaceutical_form)
                values (?, ?, 'Tablet', '10 mg', 'TABLET')
                """, presentationId, medicationId);
        String draft = """
                {"history":{"personal":{"allergiesAndReactions":"None recorded"}},
                 "assessment":{"presentation":{"reason":"Synthetic visit",
                    "symptomsAndCurrentIllness":"Synthetic symptoms"},
                   "diagnosis":{"primaryDiagnosis":"Synthetic primary diagnosis",
                    "icd10CodeId":"%s","diagnosisType":"PRESUMPTIVE"},
                   "treatmentPlan":{"therapeuticPlan":"Synthetic plan",
                    "generalIndications":"Synthetic instructions","referralType":"NONE"}},
                 "prescription":{"additionalPrecautions":"Synthetic precaution",
                    "nonPharmacologicalRecommendations":"Synthetic rest",
                    "items":[{"medicationId":"%s","presentationId":"%s",
                    "dose":1,"doseUnit":"MG","frequency":"ONCE","route":"ORAL",
                    "duration":1,"durationUnit":"DAYS","quantity":1,
                    "usageInstructions":"Synthetic use only"}]}}
                """.formatted(codeId, medicationId, presentationId);
        jdbc.update("insert into encounter_drafts (encounter_id, version, content) "
                + "values (?, 1, cast(? as jsonb))", encounterId, draft);

        mvc.perform(post("/medical/encounters/{id}/finalize", encounterId)
                        .with(authentication(actor(OTHER_USER, "PROFESSIONAL")))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"version\":1}"))
                .andExpect(status().isNotFound());
        mvc.perform(post("/medical/encounters/{id}/finalize", encounterId)
                        .with(authentication(actor(DOCTOR_USER, "PROFESSIONAL")))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"version\":2}"))
                .andExpect(status().isConflict());
        mvc.perform(post("/appointments/{id}/complete", todayAppointment)
                        .with(authentication(actor(DOCTOR_USER, "PROFESSIONAL"))))
                .andExpect(status().isConflict());
        assertThatThrownBy(() -> jdbc.queryForObject(
                "select capacity_appointment_complete(?)", Object.class, todayAppointment))
                .isInstanceOf(DataIntegrityViolationException.class);

        setSyntheticBusinessNow();
        try {
            for (int retry = 0; retry < 2; retry++) {
                mvc.perform(post("/medical/encounters/{id}/finalize", encounterId)
                                .with(authentication(actor(DOCTOR_USER, "PROFESSIONAL")))
                                .contentType(MediaType.APPLICATION_JSON).content("{\"version\":1}"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.encounterId").value(encounterId.toString()))
                        .andExpect(jsonPath("$.status").value("FINALIZED"))
                        .andExpect(jsonPath("$.prescriptionNumber").isNotEmpty());
            }
            assertThat(jdbc.queryForObject("select count(*) from clinical_final_records", Integer.class))
                    .isEqualTo(1);
            assertThat(jdbc.queryForObject(
                    "select patient_sex from clinical_final_records where encounter_id = ?",
                    String.class, encounterId)).isEqualTo("Femenino");
            assertThat(jdbc.queryForObject(
                    "select patient_district from clinical_final_records where encounter_id = ?",
                    String.class, encounterId)).isEqualTo("Lima");
            assertThat(jdbc.queryForObject("select count(*) from clinical_prescriptions", Integer.class))
                    .isEqualTo(1);
            mvc.perform(get("/medical/appointments/{id}/history?limit=1", otherAppointment)
                            .with(authentication(actor(OTHER_USER, "PROFESSIONAL"))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.items.length()").value(1))
                    .andExpect(jsonPath("$.items[0].encounterId").value(encounterId.toString()))
                    .andExpect(jsonPath("$.items[0].reason").value("Synthetic visit"))
                    .andExpect(jsonPath("$.items[0].primaryDiagnosis").value("Synthetic primary diagnosis"))
                    .andExpect(jsonPath("$.items[0].simulatedService")
                            .value("Servicio ambulatorio (dato simulado)"))
                    .andExpect(jsonPath("$.hasMore").value(false));
            mvc.perform(get("/medical/appointments/{id}/history/{priorId}", otherAppointment, encounterId)
                            .with(authentication(actor(OTHER_USER, "PROFESSIONAL"))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.encounter.primaryDiagnosis").value("Synthetic primary diagnosis"))
                    .andExpect(jsonPath("$.history.allergies_and_reactions").value("None recorded"))
                    .andExpect(jsonPath("$.prescription.items.length()").value(1));
            mvc.perform(get("/medical/appointments/{id}/history/{priorId}", otherAppointment, UUID.randomUUID())
                            .with(authentication(actor(OTHER_USER, "PROFESSIONAL"))))
                    .andExpect(status().isNotFound());
            mvc.perform(get("/medical/appointments/{id}/history/{priorId}", otherAppointment, encounterId)
                            .with(authentication(actor(OTHER_USER, "RECEPTIONIST"))))
                    .andExpect(status().isForbidden());
            mvc.perform(get("/medical/appointments/{id}/history?limit=1&offset=1", otherAppointment)
                            .with(authentication(actor(OTHER_USER, "PROFESSIONAL"))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.items.length()").value(0));
            assertThat(jdbc.queryForObject("select count(*) from clinical_prescription_items", Integer.class))
                    .isEqualTo(1);
            assertThat(jdbc.queryForObject(
                    "select appointment_status from appointments where id = ?", String.class, todayAppointment))
                    .isEqualTo("COMPLETED");
            assertThat(jdbc.queryForObject(
                    "select flow_stage from appointments where id = ?", String.class, todayAppointment))
                    .isEqualTo("FINISHED");
            assertThat(jdbc.queryForObject("""
                    select count(*) from audit_logs where action = 'CLINICAL_ENCOUNTER_FINALIZED'
                    """, Integer.class)).isEqualTo(1);
            UUID prescriptionId = jdbc.queryForObject(
                    "select id from clinical_prescriptions where encounter_id = ?", UUID.class, encounterId);
            mvc.perform(get("/medical/me/encounters")
                            .with(authentication(actor(PATIENT_USER, "PATIENT"))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.items.length()").value(1))
                    .andExpect(jsonPath("$.items[0].encounterId").value(encounterId.toString()))
                    .andExpect(jsonPath("$.items[0].primaryDiagnosis").value("Synthetic primary diagnosis"))
                    .andExpect(jsonPath("$.hasMore").value(false));
            mvc.perform(get("/medical/me/encounters/{id}", encounterId)
                            .with(authentication(actor(PATIENT_USER, "PATIENT"))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.patientSex").value("Femenino"))
                    .andExpect(jsonPath("$.patientDistrict").value("Lima"))
                    .andExpect(jsonPath("$.reason").value("Synthetic visit"))
                    .andExpect(jsonPath("$.symptomsAndCurrentIllness").value("Synthetic symptoms"))
                    .andExpect(jsonPath("$.generalIndications").value("Synthetic instructions"));
            mvc.perform(get("/medical/me/prescriptions")
                            .with(authentication(actor(PATIENT_USER, "PATIENT"))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.items.length()").value(1))
                    .andExpect(jsonPath("$.items[0].prescriptionId").value(prescriptionId.toString()));
            mvc.perform(get("/medical/me/prescriptions/{id}", prescriptionId)
                            .with(authentication(actor(PATIENT_USER, "PATIENT"))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.items.length()").value(1))
                    .andExpect(jsonPath("$.items[0].genericName").value("Synthetic medicine"))
                    .andExpect(jsonPath("$.additionalPrecautions").value("Synthetic precaution"))
                    .andExpect(jsonPath("$.nonPharmacologicalRecommendations").value("Synthetic rest"))
                    .andExpect(jsonPath("$.items[0].concentration").value("10 mg"));
            jdbc.update("update medications set generic_name = 'Changed catalog label' where id = ?",
                    medicationId);
            jdbc.update("update icd10_codes set description = 'Changed diagnosis label' where id = ?",
                    codeId);
            jdbc.update("update patients set sex = 'Masculino', district = 'Callao' where id = ?", PATIENT);
            mvc.perform(get("/medical/me/prescriptions/{id}", prescriptionId)
                            .with(authentication(actor(PATIENT_USER, "PATIENT"))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.items[0].genericName").value("Synthetic medicine"));
            mvc.perform(get("/medical/me/encounters/{id}", encounterId)
                            .with(authentication(actor(PATIENT_USER, "PATIENT"))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.patientSex").value("Femenino"))
                    .andExpect(jsonPath("$.patientDistrict").value("Lima"))
                    .andExpect(jsonPath("$.icd10Description").value("Synthetic diagnosis"));

            UUID anotherUser = UUID.randomUUID();
            UUID anotherPatient = UUID.randomUUID();
            jdbc.update("""
                    insert into users (id, username, email, password_hash, first_name, last_name)
                    values (?, 'other-clinical-patient', 'other-clinical-patient@example.test', 'test', 'Other', 'Patient')
                    """, anotherUser);
            jdbc.update("""
                    insert into patients (id, user_id, document_type, document_number)
                    values (?, ?, 'DNI', '96000002')
                    """, anotherPatient, anotherUser);
            mvc.perform(get("/medical/me/encounters")
                            .with(authentication(actor(anotherUser, "PATIENT"))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.items.length()").value(0));
            mvc.perform(get("/medical/me/encounters/{id}", encounterId)
                            .with(authentication(actor(anotherUser, "PATIENT"))))
                    .andExpect(status().isNotFound());
            mvc.perform(get("/medical/me/prescriptions/{id}", prescriptionId)
                            .with(authentication(actor(anotherUser, "PATIENT"))))
                    .andExpect(status().isNotFound());
            for (String role : new String[] {"ADMIN", "RECEPTIONIST", "PROFESSIONAL"}) {
                mvc.perform(get("/medical/me/encounters")
                                .with(authentication(actor(DOCTOR_USER, role))))
                        .andExpect(status().isForbidden());
                mvc.perform(get("/medical/me/prescriptions/{id}", prescriptionId)
                                .with(authentication(actor(DOCTOR_USER, role))))
                        .andExpect(status().isForbidden());
            }
            mvc.perform(get("/medical/me/encounters?limit=0")
                            .with(authentication(actor(PATIENT_USER, "PATIENT"))))
                    .andExpect(status().isBadRequest());
            mvc.perform(get("/medical/me/prescriptions?offset=-1")
                            .with(authentication(actor(PATIENT_USER, "PATIENT"))))
                    .andExpect(status().isBadRequest());
            mvc.perform(post("/medical/encounters/{id}/finalize", encounterId)
                            .with(authentication(actor(DOCTOR_USER, "PROFESSIONAL")))
                            .contentType(MediaType.APPLICATION_JSON).content("{\"version\":2}"))
                    .andExpect(status().isConflict());
            jdbc.update("""
                    update encounter_drafts
                    set content = jsonb_set(content, '{assessment,presentation,reason}', '"Altered"'::jsonb)
                    where encounter_id = ?
                    """, encounterId);
            mvc.perform(post("/medical/encounters/{id}/finalize", encounterId)
                            .with(authentication(actor(DOCTOR_USER, "PROFESSIONAL")))
                            .contentType(MediaType.APPLICATION_JSON).content("{\"version\":1}"))
                    .andExpect(status().isConflict());
        } finally {
            restoreBusinessNow();
        }
    }

    @Test
    void rollsBackFinalRowsAndStatusWhenCompletionFailsAndDoesNotCreateEmptyPrescription() throws Exception {
        UUID encounterId = startTodayAppointment();
        UUID sourceId = UUID.randomUUID();
        UUID codeId = UUID.randomUUID();
        jdbc.update("""
                insert into medical_catalog_sources
                    (id, catalog_type, source_name, source_version, license_reference,
                     approved_by_user_id, approved_at)
                values (?, 'ICD10', 'Synthetic rollback ICD', '1', 'TEST ONLY', ?, current_timestamp)
                """, sourceId, DOCTOR_USER);
        jdbc.update("insert into icd10_codes (id, code, description, source_id) "
                + "values (?, 'SYN-ROLLBACK', 'Synthetic rollback diagnosis', ?)", codeId, sourceId);
        String draft = """
                {"history":{},"assessment":{
                    "presentation":{"reason":"Synthetic visit","symptomsAndCurrentIllness":"Synthetic symptoms"},
                    "diagnosis":{"primaryDiagnosis":"Synthetic diagnosis","icd10CodeId":"%s",
                        "diagnosisType":"DEFINITIVE"},
                    "treatmentPlan":{"therapeuticPlan":"Synthetic plan",
                        "generalIndications":"Synthetic indications"}},
                 "prescription":{"items":[]}}
                """.formatted(codeId);
        jdbc.update("insert into encounter_drafts (encounter_id, version, content) "
                + "values (?, 1, cast(? as jsonb))", encounterId, draft);
        jdbc.update("update icd10_codes set active = false where id = ?", codeId);
        mvc.perform(post("/medical/encounters/{id}/finalize", encounterId)
                        .with(authentication(actor(DOCTOR_USER, "PROFESSIONAL")))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"version\":1}"))
                .andExpect(status().isBadRequest());
        assertThat(jdbc.queryForObject("select count(*) from clinical_final_records", Integer.class)).isZero();
        jdbc.update("update icd10_codes set active = true where id = ?", codeId);

        setSyntheticBusinessNow();
        try {
            jdbc.execute("""
                    create function reject_test_completion() returns trigger language plpgsql as $$
                    begin raise exception 'Synthetic completion failure' using errcode = '23514'; end $$
                    """);
            jdbc.execute("""
                    create trigger trg_test_completion_failure
                    before update of appointment_status on appointments
                    for each row when (new.appointment_status = 'COMPLETED')
                    execute function reject_test_completion()
                    """);
            SecurityContextHolder.getContext().setAuthentication(actor(DOCTOR_USER, "PROFESSIONAL"));
            try {
                assertThatThrownBy(() -> finalization.finalizeEncounter(encounterId, 1))
                        .isInstanceOf(DataIntegrityViolationException.class);
            } finally {
                SecurityContextHolder.clearContext();
            }
            assertThat(jdbc.queryForObject("select count(*) from clinical_final_records", Integer.class)).isZero();
            assertThat(jdbc.queryForObject("select count(*) from clinical_diagnoses", Integer.class)).isZero();
            assertThat(jdbc.queryForObject("select count(*) from clinical_prescriptions", Integer.class)).isZero();
            assertThat(jdbc.queryForObject(
                    "select status from clinical_encounters where id = ?", String.class, encounterId))
                    .isEqualTo("OPEN");
            assertThat(jdbc.queryForObject(
                    "select appointment_status from appointments where id = ?", String.class, todayAppointment))
                    .isEqualTo("CONFIRMED");
            jdbc.execute("drop trigger trg_test_completion_failure on appointments");
            jdbc.execute("drop function reject_test_completion()");

            mvc.perform(post("/medical/encounters/{id}/finalize", encounterId)
                            .with(authentication(actor(DOCTOR_USER, "PROFESSIONAL")))
                            .contentType(MediaType.APPLICATION_JSON).content("{\"version\":1}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.prescriptionNumber").isEmpty());
            assertThat(jdbc.queryForObject("select count(*) from clinical_prescriptions", Integer.class)).isZero();
            mvc.perform(get("/medical/me/encounters")
                            .with(authentication(actor(PATIENT_USER, "PATIENT"))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.items.length()").value(1));
            mvc.perform(get("/medical/me/prescriptions")
                            .with(authentication(actor(PATIENT_USER, "PATIENT"))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.items.length()").value(0));
        } finally {
            jdbc.execute("drop trigger if exists trg_test_completion_failure on appointments");
            jdbc.execute("drop function if exists reject_test_completion()");
            restoreBusinessNow();
        }
    }

    private void setSyntheticBusinessNow() {
        jdbc.execute("""
                create or replace function hospital_business_now() returns timestamp
                language sql volatile security definer set search_path=pg_catalog as
                $$ select timestamp '%s 09:00:00' $$
                """.formatted(APPOINTMENT_DATE));
    }

    private void restoreBusinessNow() {
        jdbc.execute("""
                create or replace function hospital_business_now() returns timestamp
                language plpgsql volatile security definer set search_path=pg_catalog as $$
                declare v_zone text;
                begin
                    select zone_name into strict v_zone
                    from public.hospital_business_config where singleton;
                    return clock_timestamp() at time zone v_zone;
                end $$
                """);
    }

    private UUID startTodayAppointment() throws Exception {
        mvc.perform(post("/medical/appointments/{id}/start", todayAppointment)
                        .with(authentication(actor(DOCTOR_USER, "PROFESSIONAL"))))
                .andExpect(status().isOk());
        return jdbc.queryForObject("select id from clinical_encounters where appointment_id = ?",
                UUID.class, todayAppointment);
    }

    private void insertSlot(UUID slot, UUID schedule, LocalDate date, String start) {
        jdbc.update("insert into availability_slots "
                + "(id, schedule_id, slot_date, start_time, end_time, status) "
                + "values (?, ?, ?, cast(? as time), cast(? as time) + interval '30 minutes', 'AVAILABLE')",
                slot, schedule, Date.valueOf(date), start, start);
    }

    private UUID insertWaitingAppointment(UUID slot) {
        UUID appointment = jdbc.queryForObject("select capacity_reserve(?,?,?)", UUID.class,
                slot, PATIENT, "Clinical context fixture");
        jdbc.queryForObject("select capacity_appointment_confirm(?)", Object.class, appointment);
        jdbc.queryForObject("select capacity_appointment_stage(?, 'CHECK_IN')", Object.class, appointment);
        jdbc.queryForObject("select capacity_appointment_stage(?, 'WAITING')", Object.class, appointment);
        return appointment;
    }

    private UsernamePasswordAuthenticationToken actor(UUID userId, String role) {
        AuthenticatedUser user = new AuthenticatedUser(userId, "medical-context@example.test", "medical-context",
                "test", true, Set.of(role), Set.of());
        return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    }
}
