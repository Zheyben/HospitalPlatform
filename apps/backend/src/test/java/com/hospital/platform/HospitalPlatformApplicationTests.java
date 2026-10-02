package com.hospital.platform;

import com.hospital.platform.auth.repository.RefreshTokenRepository;
import com.hospital.platform.agenda.repository.AvailabilitySlotRepository;
import com.hospital.platform.agenda.repository.ScheduleRepository;
import com.hospital.platform.agenda.contract.CapacityGateway;
import com.hospital.platform.agenda.contract.PostgresCapacityGateway;
import com.hospital.platform.appointments.repository.AppointmentRepository;
import com.hospital.platform.patients.repository.PatientRepository;
import com.hospital.platform.professionals.repository.ProfessionalRepository;
import com.hospital.platform.users.repository.RoleRepository;
import com.hospital.platform.users.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = {
        "spring.autoconfigure.exclude="
                + "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration"
})
@ActiveProfiles("test")
class HospitalPlatformApplicationTests {

    @Test
    void contextLoads() {
    }

    @TestConfiguration
    static class TestSecurityConfiguration {

        @Bean
        UserRepository userRepository() {
            return Mockito.mock(UserRepository.class);
        }

        @Bean
        RefreshTokenRepository refreshTokenRepository() {
            return Mockito.mock(RefreshTokenRepository.class);
        }

        @Bean
        RoleRepository roleRepository() {
            return Mockito.mock(RoleRepository.class);
        }

        @Bean
        PatientRepository patientRepository() {
            return Mockito.mock(PatientRepository.class);
        }

        @Bean
        ProfessionalRepository professionalRepository() {
            return Mockito.mock(ProfessionalRepository.class);
        }

        @Bean
        ScheduleRepository scheduleRepository() {
            return Mockito.mock(ScheduleRepository.class);
        }

        @Bean
        AvailabilitySlotRepository availabilitySlotRepository() {
            return Mockito.mock(AvailabilitySlotRepository.class);
        }

        @Bean
        AppointmentRepository appointmentRepository() {
            return Mockito.mock(AppointmentRepository.class);
        }

        @Bean
        org.springframework.jdbc.core.JdbcTemplate jdbcTemplate() {
            return Mockito.mock(org.springframework.jdbc.core.JdbcTemplate.class);
        }

        @Bean
        jakarta.persistence.EntityManager entityManager() {
            return Mockito.mock(jakarta.persistence.EntityManager.class);
        }
    }
}
