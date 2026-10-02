package com.hospital.platform.agenda.demo;

import com.hospital.platform.agenda.service.SlotGenerationService;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Component
@Profile("dev")
@ConditionalOnProperty(name = "hospital.demo.seed-enabled", havingValue = "true")
public class DevDemoDataSeeder implements ApplicationRunner {
    private final JdbcTemplate jdbc;
    private final PasswordEncoder passwords;
    private final SlotGenerationService slotGeneration;
    private final TransactionTemplate transaction;

    public DevDemoDataSeeder(JdbcTemplate jdbc, PasswordEncoder passwords,
            SlotGenerationService slotGeneration, PlatformTransactionManager transactionManager) {
        this.jdbc = jdbc;
        this.passwords = passwords;
        this.slotGeneration = slotGeneration;
        this.transaction = new TransactionTemplate(transactionManager);
    }

    @Override
    public void run(ApplicationArguments args) {
        transaction.executeWithoutResult(ignored -> seed());
    }

    private void seed() {
        UUID specialtyId = id("specialty");
        UUID userId = id("professional-user");
        UUID professionalId = id("professional");

        jdbc.update("""
                insert into specialties (id, name, description) values (?, 'Medicina general', 'Demo de desarrollo')
                on conflict (id) do nothing
                """, specialtyId);
        jdbc.update("""
                insert into roles (id, name, description) values (?, 'PROFESSIONAL', 'Professional account')
                on conflict (name) do nothing
                """, id("professional-role"));
        // Enabled account with an unknown random password: only the name is shown in the demo.
        jdbc.update("""
                insert into users (id, username, email, password_hash, first_name, last_name, enabled)
                values (?, 'demo-professional', 'demo.professional@hospital.local', ?, 'Elena', 'Vargas', true)
                on conflict (id) do update set enabled=true
                """, userId, passwords.encode(UUID.randomUUID().toString()));
        jdbc.update("""
                insert into user_roles (user_id, role_id)
                select ?, id from roles where name = 'PROFESSIONAL'
                on conflict do nothing
                """, userId);
        jdbc.update("""
                insert into professionals (id, user_id, license_number)
                values (?, ?, '900001') on conflict (id) do nothing
                """, professionalId, userId);
        jdbc.update("""
                insert into professional_specialties (professional_id, specialty_id)
                values (?, ?) on conflict do nothing
                """, professionalId, specialtyId);

        for (int day = 0; day <= 6; day++) {
            UUID scheduleId = id("schedule-" + day);
            jdbc.update("""
                    insert into schedules (id, professional_id, specialty_id, day_of_week, start_time, end_time)
                    values (?, ?, ?, ?, time '09:00', time '12:00') on conflict (id) do nothing
                    """, scheduleId, professionalId, specialtyId, day);
            slotGeneration.generate(scheduleId);
        }
    }

    private UUID id(String value) {
        return UUID.nameUUIDFromBytes(("hospital-platform-dev-demo:" + value)
                .getBytes(StandardCharsets.UTF_8));
    }
}
