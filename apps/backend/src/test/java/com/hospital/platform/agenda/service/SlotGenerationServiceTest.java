package com.hospital.platform.agenda.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hospital.platform.agenda.contract.CapacityGateway;
import com.hospital.platform.agenda.entity.Schedule;
import com.hospital.platform.agenda.repository.AvailabilitySlotRepository;
import com.hospital.platform.agenda.repository.ScheduleRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SlotGenerationServiceTest {
    private static final UUID SCHEDULE_ID = UUID.randomUUID();
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-09-30T12:00:00Z"), ZoneOffset.UTC);

    @Mock ScheduleRepository schedules;
    @Mock CapacityGateway capacityGateway;

    @Test
    void generatesOnlyMatchingFutureWeekdaysInThirtyMinuteIntervals() {
        when(schedules.findById(SCHEDULE_ID)).thenReturn(Optional.of(schedule(4, "09:00", "10:10")));
        when(capacityGateway.generateSlot(any(), any(), any(), any())).thenReturn(true);

        assertThat(service().generate(SCHEDULE_ID)).isEqualTo(4);

        ArgumentCaptor<LocalDate> dates = ArgumentCaptor.forClass(LocalDate.class);
        ArgumentCaptor<LocalTime> starts = ArgumentCaptor.forClass(LocalTime.class);
        ArgumentCaptor<LocalTime> ends = ArgumentCaptor.forClass(LocalTime.class);
        verify(capacityGateway, org.mockito.Mockito.times(4)).generateSlot(
                eq(SCHEDULE_ID), dates.capture(), starts.capture(), ends.capture());
        assertThat(dates.getAllValues()).containsExactly(
                LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 8), LocalDate.of(2026, 10, 8));
        assertThat(starts.getAllValues()).containsExactly(
                LocalTime.of(9, 0), LocalTime.of(9, 30), LocalTime.of(9, 0), LocalTime.of(9, 30));
        assertThat(ends.getAllValues()).containsExactly(
                LocalTime.of(9, 30), LocalTime.of(10, 0), LocalTime.of(9, 30), LocalTime.of(10, 0));
    }

    @Test
    void returnsZeroWhenRepositorySkipsExistingSlots() {
        when(schedules.findById(SCHEDULE_ID)).thenReturn(Optional.of(schedule(4, "09:00", "09:30")));
        when(capacityGateway.generateSlot(any(), any(), any(), any())).thenReturn(true, true, false, false);
        assertThat(service().generate(SCHEDULE_ID)).isEqualTo(2);
        assertThat(service().generate(SCHEDULE_ID)).isZero();
    }

    @Test
    void doesNotGenerateForInactiveSchedule() {
        Schedule inactive = schedule(4, "09:00", "10:00");
        inactive.changeStatus(false);
        when(schedules.findById(SCHEDULE_ID)).thenReturn(Optional.of(inactive));
        assertThat(service().generate(SCHEDULE_ID)).isZero();
        verify(capacityGateway, never()).generateSlot(any(), any(), any(), any());
    }

    @Test
    void sundayUsesZeroConvention() {
        when(schedules.findById(SCHEDULE_ID)).thenReturn(Optional.of(schedule(0, "09:00", "09:30")));
        when(capacityGateway.generateSlot(any(), any(), any(), any())).thenReturn(true);
        assertThat(service().generate(SCHEDULE_ID)).isEqualTo(2);
        verify(capacityGateway).generateSlot(eq(SCHEDULE_ID), eq(LocalDate.of(2026, 10, 4)),
                eq(LocalTime.of(9, 0)), eq(LocalTime.of(9, 30)));
    }

    private SlotGenerationService service() {
        return new SlotGenerationService(schedules, capacityGateway, CLOCK);
    }

    private Schedule schedule(int day, String start, String end) {
        return new Schedule(SCHEDULE_ID, UUID.randomUUID(), UUID.randomUUID(), day,
                LocalTime.parse(start), LocalTime.parse(end));
    }
}
