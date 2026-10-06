package com.hospital.platform.agenda;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.platform.agenda.service.SlotGenerationService;
import java.time.LocalDate;
import java.time.Clock;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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
class PatientAvailabilityFlowIT {
    @Container
    static final PostgreSQLContainer postgres = new PostgreSQLContainer(DockerImageName.parse("postgres:16-alpine"))
            .withDatabaseName("hospital_platform_b2_test")
            .withUsername("hospital_app_test")
            .withPassword("hospital_app_test");

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("hospital.security.jwt-secret", () -> "12345678901234567890123456789012");
    }

    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;
    @Autowired ObjectMapper json;
    @Autowired SlotGenerationService generation;
    @Autowired Clock clock;

    @Test
    void patientSeesOnlyOperationalCapacityAndBooksExactlyOnce() throws Exception {
        UUID specialty = UUID.randomUUID();
        UUID inactiveSpecialty = UUID.randomUUID();
        UUID professionalUser = UUID.randomUUID();
        UUID professional = UUID.randomUUID();
        UUID inactiveProfessional = UUID.randomUUID();
        UUID secondProfessional = UUID.randomUUID();
        UUID schedule = UUID.randomUUID();
        UUID inactiveSchedule = UUID.randomUUID();
        UUID inactiveProfessionalSchedule = UUID.randomUUID();
        UUID inactiveSpecialtySchedule = UUID.randomUUID();
        LocalDate tomorrow = LocalDate.now(clock).plusDays(1);
        int tomorrowDow = tomorrow.getDayOfWeek().getValue() % 7;

        jdbc.update("insert into specialties (id, name) values (?, 'B2 Active')", specialty);
        jdbc.update("insert into specialties (id, name) values (?, 'B2 Inactive')", inactiveSpecialty);
        jdbc.update("""
                insert into users (id, username, email, password_hash, first_name, last_name, enabled)
                values (?, 'b2-pro', 'b2-pro@example.test', 'disabled', 'Elena', 'Vargas', true)
                """, professionalUser);
        jdbc.update("insert into professionals (id, user_id, license_number) values (?, ?, '910006')",
                professional, professionalUser);
        jdbc.update("insert into professionals (id, license_number) values (?, '910007')", inactiveProfessional);
        jdbc.update("insert into professionals (id, license_number) values (?, '910008')", secondProfessional);
        associate(professional, specialty);
        associate(inactiveProfessional, specialty);
        associate(secondProfessional, inactiveSpecialty);
        insertSchedule(schedule, professional, specialty, tomorrowDow, "09:00", "10:00");
        insertSchedule(inactiveSchedule, professional, specialty, tomorrowDow, "10:00", "11:00");
        insertSchedule(inactiveProfessionalSchedule, inactiveProfessional, specialty,
                tomorrowDow, "09:00", "10:00");
        insertSchedule(inactiveSpecialtySchedule, secondProfessional, inactiveSpecialty,
                tomorrowDow, "09:00", "10:00");
        assertThat(jdbc.queryForObject("select extract(dow from cast(? as date))::int", Integer.class, tomorrow))
                .isEqualTo(tomorrowDow);

        for (UUID id : new UUID[]{schedule, inactiveSchedule, inactiveProfessionalSchedule,
                inactiveSpecialtySchedule}) {
            assertThat(generation.generate(id)).isEqualTo(4);
            assertThat(generation.generate(id)).isZero();
        }

        UUID reserved = slot(schedule, tomorrow, "09:00");
        UUID blocked = slot(schedule, tomorrow, "09:30");
        UUID hiddenScheduleSlot = slot(inactiveSchedule, tomorrow, "10:00");
        UUID hiddenProfessionalSlot = slot(inactiveProfessionalSchedule, tomorrow, "09:00");
        UUID hiddenSpecialtySlot = slot(inactiveSpecialtySchedule, tomorrow, "09:00");
        jdbc.update("update availability_slots set status='BLOCKED' where id=?", blocked);
        jdbc.update("update schedules set active=false where id=?", inactiveSchedule);
        jdbc.update("update professionals set deleted_at=now() where id=?", inactiveProfessional);
        jdbc.update("update specialties set active=false where id=?", inactiveSpecialty);

        mvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON).content("""
                {"email":"b2-patient@example.test","password":"strong-password","documentType":"DNI",
                 "documentNumber":"92000001","firstName":"Ana","lastName":"Demo",
                 "birthDate":"1990-01-01","phone":"3001234567","insurance":"SIS"}
                """)).andExpect(status().isCreated());
        String loginBody = mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"b2-patient@example.test\",\"password\":\"strong-password\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String token = json.readTree(loginBody).get("accessToken").asText();
        UUID patientId = jdbc.queryForObject("""
                select p.id from patients p join users u on u.id=p.user_id where u.email='b2-patient@example.test'
                """, UUID.class);

        mvc.perform(get("/patients/search").with(user("reception").roles("RECEPTIONIST"))
                        .param("documentType", " dni ").param("documentNumber", " 92000001 "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.patientId").value(patientId.toString()))
                .andExpect(jsonPath("$.patientName").value("Ana Demo"))
                .andExpect(jsonPath("$.phone").doesNotExist());
        UUID patientWithoutUser = UUID.randomUUID();
        jdbc.update("insert into patients (id, document_type, document_number) values (?, 'DNI', '92000002')",
                patientWithoutUser);
        mvc.perform(get("/patients/search").with(user("reception").roles("RECEPTIONIST"))
                        .param("documentType", "DNI").param("documentNumber", "92000002"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.patientId").value(patientWithoutUser.toString()));
        jdbc.update("update patients set deleted_at=now() where id=?", patientWithoutUser);
        mvc.perform(get("/patients/search").with(user("reception").roles("RECEPTIONIST"))
                        .param("documentType", "DNI").param("documentNumber", "92000002"))
                .andExpect(status().isNotFound());
        mvc.perform(get("/patients/search").with(user("reception").roles("RECEPTIONIST"))
                        .param("documentType", "DNI").param("documentNumber", "bad"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/patients/search").with(user("patient").roles("PATIENT"))
                        .param("documentType", "DNI").param("documentNumber", "92000001"))
                .andExpect(status().isForbidden());
        mvc.perform(get("/patients/search").param("documentType", "DNI")
                        .param("documentNumber", "92000001"))
                .andExpect(status().isUnauthorized());
        jdbc.queryForObject("select capacity_reserve(?,?,?)", UUID.class,
                reserved, patientId, "Existing booking");

        JsonNode availability = json.readTree(mvc.perform(get("/availability")
                        .header("Authorization", "Bearer " + token).param("status", "BLOCKED"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
        assertThat(availability).hasSize(2);
        for (JsonNode slot : availability) {
            assertThat(slot.get("status").asText()).isEqualTo("AVAILABLE");
            assertThat(slot.get("slotId").asText()).isEqualTo(slot.get("id").asText());
            assertThat(slot.get("slotDate").asText()).isEqualTo(tomorrow.plusDays(7).toString());
            assertThat(slot.get("professionalId").asText()).isEqualTo(professional.toString());
            assertThat(slot.get("professionalName").asText()).isEqualTo("Elena Vargas");
            assertThat(slot.get("specialtyId").asText()).isEqualTo(specialty.toString());
            assertThat(slot.get("specialtyName").asText()).isEqualTo("B2 Active");
        }
        mvc.perform(get("/availability").header("Authorization", "Bearer " + token)
                        .param("slotDate", tomorrow.minusDays(2).toString()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
        mvc.perform(get("/availability").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(16));
        mvc.perform(get("/availability").with(user("reception").roles("RECEPTIONIST")))
                .andExpect(status().isForbidden());
        mvc.perform(get("/reception/availability")
                        .with(user("reception").roles("RECEPTIONIST"))
                        .param("slotDate", tomorrow.plusDays(7).toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].status").value("AVAILABLE"))
                .andExpect(jsonPath("$[0].professionalName").value("Elena Vargas"));
        mvc.perform(get("/reception/availability").with(user("patient").roles("PATIENT")))
                .andExpect(status().isForbidden());
        mvc.perform(get("/reception/availability"))
                .andExpect(status().isUnauthorized());

        UUID slotId = UUID.fromString(availability.get(0).get("id").asText());
        String request = "{\"slotId\":\"" + slotId + "\",\"reason\":\"Consulta de demostración\"}";
        JsonNode appointment = json.readTree(mvc.perform(post("/appointments")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.appointmentStatus").value("SCHEDULED"))
                .andReturn().getResponse().getContentAsString());
        assertThat(appointment.get("patientId").asText()).isEqualTo(patientId.toString());
        assertThat(appointment.get("professionalId").asText()).isEqualTo(professional.toString());
        assertThat(jdbc.queryForObject("select status from availability_slots where id=?", String.class, slotId))
                .isEqualTo("RESERVED");
        mvc.perform(post("/appointments").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(request))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("SLOT_UNAVAILABLE"));
        for (UUID hidden : new UUID[]{reserved, blocked, hiddenScheduleSlot,
                hiddenProfessionalSlot, hiddenSpecialtySlot}) {
            mvc.perform(post("/appointments").header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"slotId\":\"" + hidden + "\"}"))
                    .andExpect(status().isConflict());
        }

        UUID receptionSlot = UUID.fromString(availability.get(1).get("slotId").asText());
        mvc.perform(post("/appointments").with(user("reception").roles("RECEPTIONIST"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"patientId\":\"" + patientId + "\",\"slotId\":\""
                                + receptionSlot + "\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.patientId").value(patientId.toString()))
                .andExpect(jsonPath("$.appointmentStatus").value("SCHEDULED"));
        mvc.perform(get("/reception/availability").with(user("reception").roles("RECEPTIONIST"))
                        .param("slotDate", tomorrow.plusDays(7).toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    private void associate(UUID professional, UUID specialty) {
        jdbc.update("insert into professional_specialties (professional_id, specialty_id) values (?, ?)",
                professional, specialty);
    }

    private void insertSchedule(UUID id, UUID professional, UUID specialty, int day,
                                String start, String end) {
        jdbc.update("""
                insert into schedules (id, professional_id, specialty_id, day_of_week, start_time, end_time, active)
                values (?, ?, ?, ?, cast(? as time), cast(? as time), true)
                """, id, professional, specialty, day, start, end);
    }

    private UUID slot(UUID schedule, LocalDate date, String time) {
        return jdbc.queryForObject("""
                select id from availability_slots where schedule_id=? and slot_date=? and start_time=cast(? as time)
                """, UUID.class, schedule, date, time);
    }
}
