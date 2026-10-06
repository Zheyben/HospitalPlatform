package com.hospital.platform.medical;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;

import com.hospital.platform.medical.dto.MedicalAppointmentContextDTO;
import com.hospital.platform.medical.dto.MedicalPriorEncounterDTO;
import com.hospital.platform.medical.repository.MedicalContextRepository;
import com.hospital.platform.medical.repository.MedicalContextRepository.ProfessionalRow;
import com.hospital.platform.medical.service.MedicalAppointmentNotFoundException;
import com.hospital.platform.medical.service.MedicalContextService;
import com.hospital.platform.medical.service.InvalidMedicalHistoryPageException;
import com.hospital.platform.users.service.AuthenticatedUser;
import com.hospital.platform.users.service.CurrentUserService;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

class MedicalContextServiceTest {

    private static final UUID USER = UUID.randomUUID();
    private static final UUID PROFESSIONAL = UUID.randomUUID();
    private static final UUID SPECIALTY = UUID.randomUUID();
    private static final UUID APPOINTMENT = UUID.randomUUID();
    private static final ZoneId LIMA = ZoneId.of("America/Lima");
    private final MedicalContextRepository repository = Mockito.mock(MedicalContextRepository.class);
    private final CurrentUserService currentUser = Mockito.mock(CurrentUserService.class);
    private final Clock clock = Clock.fixed(Instant.parse("2026-10-06T04:30:00Z"), ZoneId.of("UTC"));
    private final MedicalContextService service = new MedicalContextService(repository, currentUser, clock);

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void usesLimaDateAndAuthenticatedProfessionalOnly() {
        authenticate("PROFESSIONAL");
        when(currentUser.currentUserId()).thenReturn(USER);
        when(repository.findActiveProfessional(USER)).thenReturn(Optional.of(professional()));
        when(repository.findReadyAppointments(PROFESSIONAL, LocalDate.of(2026, 10, 5)))
                .thenReturn(List.of());

        assertThat(service.ownContext().professionalId()).isEqualTo(PROFESSIONAL);
        verify(repository).findReadyAppointments(PROFESSIONAL, LocalDate.of(2026, 10, 5));
        assertThat(LocalDate.now(clock.withZone(LIMA))).isEqualTo(LocalDate.of(2026, 10, 5));
    }

    @Test
    void rejectsNonProfessionalBeforeReadingRepository() {
        authenticate("RECEPTIONIST");

        assertThatThrownBy(service::ownContext).isInstanceOf(AccessDeniedException.class);
        verifyNoInteractions(repository);
    }

    @Test
    void hidesUnassignedAppointmentAsNotFound() {
        authenticate("PROFESSIONAL");
        when(currentUser.currentUserId()).thenReturn(USER);
        when(repository.findActiveProfessional(USER)).thenReturn(Optional.of(professional()));
        when(repository.findAssignedAppointment(PROFESSIONAL, APPOINTMENT, LocalDate.of(2026, 10, 5)))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.appointmentContext(APPOINTMENT))
                .isInstanceOf(MedicalAppointmentNotFoundException.class);
    }

    @Test
    void historyRequiresAssignedAppointmentBeforeReadingClinicalRows() {
        authenticate("PROFESSIONAL");
        when(currentUser.currentUserId()).thenReturn(USER);
        when(repository.findActiveProfessional(USER)).thenReturn(Optional.of(professional()));
        when(repository.findAssignedAppointment(PROFESSIONAL, APPOINTMENT, LocalDate.of(2026, 10, 5)))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.priorEncounters(APPOINTMENT, 20, 0))
                .isInstanceOf(MedicalAppointmentNotFoundException.class);
        verify(repository, never()).findPriorEncounters(APPOINTMENT, PROFESSIONAL,
                LocalDate.of(2026, 10, 5), 21, 0);
    }

    @Test
    void historyBoundsPageAndReportsHasMore() {
        authenticate("PROFESSIONAL");
        when(currentUser.currentUserId()).thenReturn(USER);
        when(repository.findActiveProfessional(USER)).thenReturn(Optional.of(professional()));
        assertThatThrownBy(() -> service.priorEncounters(APPOINTMENT, 0, 0))
                .isInstanceOf(InvalidMedicalHistoryPageException.class);

        when(repository.findAssignedAppointment(PROFESSIONAL, APPOINTMENT, LocalDate.of(2026, 10, 5)))
                .thenReturn(Optional.of(Mockito.mock(MedicalAppointmentContextDTO.class)));
        MedicalPriorEncounterDTO first = new MedicalPriorEncounterDTO(UUID.randomUUID(), null, null,
                null, null, null, null, null, null);
        MedicalPriorEncounterDTO second = new MedicalPriorEncounterDTO(UUID.randomUUID(), null, null,
                null, null, null, null, null, null);
        when(repository.findPriorEncounters(APPOINTMENT, PROFESSIONAL, LocalDate.of(2026, 10, 5), 2, 0))
                .thenReturn(List.of(first, second));

        var page = service.priorEncounters(APPOINTMENT, 1, 0);

        assertThat(page.items()).containsExactly(first);
        assertThat(page.hasMore()).isTrue();
        verify(repository).findPriorEncounters(APPOINTMENT, PROFESSIONAL, LocalDate.of(2026, 10, 5), 2, 0);
    }

    private ProfessionalRow professional() {
        return new ProfessionalRow(PROFESSIONAL, "Ana", "Rojas", "CMP123",
                "SIM-RNE-000000000001", SPECIALTY, "General");
    }

    private void authenticate(String role) {
        AuthenticatedUser user = new AuthenticatedUser(USER, "doctor@example.test", "doctor", "test", true,
                Set.of(role), Set.of());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
    }
}
