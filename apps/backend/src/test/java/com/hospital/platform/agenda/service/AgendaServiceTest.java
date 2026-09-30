package com.hospital.platform.agenda.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hospital.platform.agenda.dto.AgendaResponseDTO;
import com.hospital.platform.agenda.dto.AvailabilitySlotResponseDTO;
import com.hospital.platform.agenda.dto.CreateAgendaRequestDTO;
import com.hospital.platform.agenda.dto.UpdateAgendaRequestDTO;
import com.hospital.platform.agenda.entity.AvailabilitySlot;
import com.hospital.platform.agenda.entity.AvailabilitySlotStatus;
import com.hospital.platform.agenda.entity.Schedule;
import com.hospital.platform.agenda.exception.AgendaNotFoundException;
import com.hospital.platform.agenda.exception.AvailabilitySlotNotFoundException;
import com.hospital.platform.agenda.exception.InvalidScheduleTimeException;
import com.hospital.platform.agenda.exception.ProfessionalNotAvailableException;
import com.hospital.platform.agenda.repository.AvailabilitySlotRepository;
import com.hospital.platform.agenda.repository.ScheduleRepository;
import com.hospital.platform.professionals.contract.ProfessionalLookupService;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AgendaServiceTest {

    private static final UUID AGENDA_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID SLOT_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID PROFESSIONAL_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID SPECIALTY_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");
    private static final LocalTime START_TIME = LocalTime.of(9, 0);
    private static final LocalTime END_TIME = LocalTime.of(12, 0);

    @Mock
    private ScheduleRepository scheduleRepository;

    @Mock
    private AvailabilitySlotRepository availabilitySlotRepository;

    @Mock
    private ProfessionalLookupService professionalLookupService;

    private AgendaService agendaService;

    @BeforeEach
    void setUp() {
        agendaService = new AgendaService(
                scheduleRepository,
                availabilitySlotRepository,
                professionalLookupService
        );
    }

    @Test
    void createsAgendaForActiveProfessional() {
        when(professionalLookupService.existsActiveProfessional(PROFESSIONAL_ID)).thenReturn(true);
        when(scheduleRepository.save(any(Schedule.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AgendaResponseDTO response = agendaService.createAgenda(createRequest());
        ArgumentCaptor<Schedule> scheduleCaptor = ArgumentCaptor.forClass(Schedule.class);

        verify(scheduleRepository).save(scheduleCaptor.capture());
        assertThat(scheduleCaptor.getValue().getProfessionalId()).isEqualTo(PROFESSIONAL_ID);
        assertThat(scheduleCaptor.getValue().getSpecialtyId()).isEqualTo(SPECIALTY_ID);
        assertThat(response.active()).isTrue();
    }

    @Test
    void rejectsAgendaForInactiveOrMissingProfessional() {
        when(professionalLookupService.existsActiveProfessional(PROFESSIONAL_ID)).thenReturn(false);

        assertThatThrownBy(() -> agendaService.createAgenda(createRequest()))
                .isInstanceOf(ProfessionalNotAvailableException.class);
    }

    @Test
    void rejectsInvalidTimeRange() {
        when(professionalLookupService.existsActiveProfessional(PROFESSIONAL_ID)).thenReturn(true);
        CreateAgendaRequestDTO request = new CreateAgendaRequestDTO(
                PROFESSIONAL_ID,
                SPECIALTY_ID,
                1,
                END_TIME,
                START_TIME
        );

        assertThatThrownBy(() -> agendaService.createAgenda(request))
                .isInstanceOf(InvalidScheduleTimeException.class);
    }

    @Test
    void findsAgendasUsingFilters() {
        when(scheduleRepository.findSchedules(PROFESSIONAL_ID, SPECIALTY_ID))
                .thenReturn(List.of(schedule()));

        List<AgendaResponseDTO> response = agendaService.findAgendas(PROFESSIONAL_ID, SPECIALTY_ID);

        assertThat(response).hasSize(1);
        assertThat(response.get(0).id()).isEqualTo(AGENDA_ID);
    }

    @Test
    void rejectsUnknownAgenda() {
        when(scheduleRepository.findById(AGENDA_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> agendaService.findAgendaById(AGENDA_ID))
                .isInstanceOf(AgendaNotFoundException.class);
    }

    @Test
    void updatesAgendaConfiguration() {
        Schedule schedule = schedule();
        when(scheduleRepository.findById(AGENDA_ID)).thenReturn(Optional.of(schedule));
        when(professionalLookupService.existsActiveProfessional(PROFESSIONAL_ID)).thenReturn(true);
        UpdateAgendaRequestDTO request = new UpdateAgendaRequestDTO(
                PROFESSIONAL_ID,
                SPECIALTY_ID,
                2,
                LocalTime.of(13, 0),
                LocalTime.of(17, 0)
        );

        AgendaResponseDTO response = agendaService.updateAgenda(AGENDA_ID, request);

        assertThat(response.dayOfWeek()).isEqualTo(2);
        assertThat(response.startTime()).isEqualTo(LocalTime.of(13, 0));
    }

    @Test
    void changesAgendaStatus() {
        Schedule schedule = schedule();
        when(scheduleRepository.findById(AGENDA_ID)).thenReturn(Optional.of(schedule));

        AgendaResponseDTO response = agendaService.changeAgendaStatus(AGENDA_ID, false);

        assertThat(response.active()).isFalse();
        assertThat(schedule.isActive()).isFalse();
    }

    @Test
    void findsAvailabilityAndExposesUsableState() {
        AvailabilitySlot slot = availableSlot(schedule());
        LocalDate slotDate = LocalDate.of(2026, 10, 1);
        when(availabilitySlotRepository.findAvailability(
                AGENDA_ID,
                PROFESSIONAL_ID,
                slotDate,
                AvailabilitySlotStatus.AVAILABLE
        )).thenReturn(List.of(slot));

        List<AvailabilitySlotResponseDTO> response = agendaService.findAvailability(
                AGENDA_ID,
                PROFESSIONAL_ID,
                slotDate,
                AvailabilitySlotStatus.AVAILABLE
        );

        assertThat(response).hasSize(1);
        assertThat(response.get(0).usable()).isTrue();
    }

    @Test
    void rejectsUnknownAvailabilitySlot() {
        when(availabilitySlotRepository.findById(SLOT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> agendaService.findAvailabilitySlotById(SLOT_ID))
                .isInstanceOf(AvailabilitySlotNotFoundException.class);
    }

    private CreateAgendaRequestDTO createRequest() {
        return new CreateAgendaRequestDTO(
                PROFESSIONAL_ID,
                SPECIALTY_ID,
                1,
                START_TIME,
                END_TIME
        );
    }

    private Schedule schedule() {
        return new Schedule(
                AGENDA_ID,
                PROFESSIONAL_ID,
                SPECIALTY_ID,
                1,
                START_TIME,
                END_TIME
        );
    }

    private AvailabilitySlot availableSlot(Schedule schedule) {
        return new AvailabilitySlot(
                SLOT_ID,
                schedule,
                LocalDate.of(2026, 10, 1),
                START_TIME,
                LocalTime.of(9, 30),
                AvailabilitySlotStatus.AVAILABLE
        );
    }
}
