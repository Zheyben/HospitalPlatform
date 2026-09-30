package com.hospital.platform.agenda.contract;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.hospital.platform.agenda.entity.AvailabilitySlot;
import com.hospital.platform.agenda.entity.AvailabilitySlotStatus;
import com.hospital.platform.agenda.entity.Schedule;
import com.hospital.platform.agenda.repository.AvailabilitySlotRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DatabaseAvailabilitySlotServiceTest {

    private static final UUID SLOT_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID SCHEDULE_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID PROFESSIONAL_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID SPECIALTY_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");

    @Mock
    private AvailabilitySlotRepository availabilitySlotRepository;

    private AvailabilitySlotService availabilitySlotService;

    @BeforeEach
    void setUp() {
        availabilitySlotService = new DatabaseAvailabilitySlotService(availabilitySlotRepository);
    }

    @Test
    void detectsExistingSlot() {
        when(availabilitySlotRepository.existsById(SLOT_ID)).thenReturn(true);

        assertThat(availabilitySlotService.existsSlot(SLOT_ID)).isTrue();
    }

    @Test
    void detectsAvailableSlotRegardlessOfScheduleState() {
        when(availabilitySlotRepository.existsByIdAndStatus(SLOT_ID, AvailabilitySlotStatus.AVAILABLE))
                .thenReturn(true);

        assertThat(availabilitySlotService.isAvailable(SLOT_ID)).isTrue();
    }

    @Test
    void detectsAvailableSlotWithActiveScheduleAsUsable() {
        when(availabilitySlotRepository.findByIdWithSchedule(SLOT_ID))
                .thenReturn(Optional.of(slot(AvailabilitySlotStatus.AVAILABLE, true)));

        assertThat(availabilitySlotService.isUsable(SLOT_ID)).isTrue();
    }

    @Test
    void rejectsAvailableSlotWithInactiveScheduleAsNotUsable() {
        when(availabilitySlotRepository.findByIdWithSchedule(SLOT_ID))
                .thenReturn(Optional.of(slot(AvailabilitySlotStatus.AVAILABLE, false)));

        assertThat(availabilitySlotService.isUsable(SLOT_ID)).isFalse();
    }

    @Test
    void rejectsReservedSlotAsNotUsable() {
        when(availabilitySlotRepository.findByIdWithSchedule(SLOT_ID))
                .thenReturn(Optional.of(slot(AvailabilitySlotStatus.RESERVED, true)));

        assertThat(availabilitySlotService.isUsable(SLOT_ID)).isFalse();
    }

    @Test
    void rejectsBlockedSlotAsNotUsable() {
        when(availabilitySlotRepository.findByIdWithSchedule(SLOT_ID))
                .thenReturn(Optional.of(slot(AvailabilitySlotStatus.BLOCKED, true)));

        assertThat(availabilitySlotService.isUsable(SLOT_ID)).isFalse();
    }

    private AvailabilitySlot slot(AvailabilitySlotStatus status, boolean scheduleActive) {
        Schedule schedule = new Schedule(
                SCHEDULE_ID,
                PROFESSIONAL_ID,
                SPECIALTY_ID,
                1,
                LocalTime.of(9, 0),
                LocalTime.of(12, 0)
        );
        if (!scheduleActive) {
            schedule.changeStatus(false);
        }
        return new AvailabilitySlot(
                SLOT_ID,
                schedule,
                LocalDate.of(2026, 10, 1),
                LocalTime.of(9, 0),
                LocalTime.of(9, 30),
                status
        );
    }
}
