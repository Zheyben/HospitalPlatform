package com.hospital.platform.agenda.service;

import com.hospital.platform.agenda.entity.Schedule;
import com.hospital.platform.agenda.contract.CapacityGateway;
import com.hospital.platform.agenda.exception.AgendaNotFoundException;
import com.hospital.platform.agenda.repository.AvailabilitySlotRepository;
import com.hospital.platform.agenda.repository.ScheduleRepository;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SlotGenerationService {
    public static final int HORIZON_DAYS = 14;
    public static final int SLOT_MINUTES = 30;

    private final ScheduleRepository schedules;
    private final CapacityGateway capacityGateway;
    private final Clock clock;

    public SlotGenerationService(ScheduleRepository schedules, CapacityGateway capacityGateway, Clock clock) {
        this.schedules = schedules;
        this.capacityGateway = capacityGateway;
        this.clock = clock;
    }

    // Database convention: 0=Sunday ... 6=Saturday (Java DayOfWeek.getValue() % 7).
    @Transactional
    public int generate(UUID scheduleId) {
        Schedule schedule = schedules.findById(scheduleId)
                .orElseThrow(() -> new AgendaNotFoundException(scheduleId));
        if (!schedule.isActive()) {
            return 0;
        }
        int created = 0;
        LocalDate today = LocalDate.now(clock);
        for (int offset = 1; offset <= HORIZON_DAYS; offset++) {
            LocalDate date = today.plusDays(offset);
            if (date.getDayOfWeek().getValue() % 7 != schedule.getDayOfWeek()) {
                continue;
            }
            for (LocalTime start = schedule.getStartTime();
                    Duration.between(start, schedule.getEndTime()).toMinutes() >= SLOT_MINUTES;
                    start = start.plusMinutes(SLOT_MINUTES)) {
                if (capacityGateway.generateSlot(scheduleId, date, start, start.plusMinutes(SLOT_MINUTES))) {
                    created++;
                }
            }
        }
        return created;
    }
}
