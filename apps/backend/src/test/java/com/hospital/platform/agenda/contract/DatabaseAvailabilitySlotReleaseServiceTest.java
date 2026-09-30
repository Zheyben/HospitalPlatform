package com.hospital.platform.agenda.contract;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.hospital.platform.agenda.repository.AvailabilitySlotRepository;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DatabaseAvailabilitySlotReleaseServiceTest {

    private static final UUID SLOT_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

    @Mock
    private AvailabilitySlotRepository availabilitySlotRepository;

    private AvailabilitySlotReleaseService releaseService;

    @BeforeEach
    void setUp() {
        releaseService = new DatabaseAvailabilitySlotReleaseService(availabilitySlotRepository);
    }

    @Test
    void releasesReservedSlotWithOneConditionalUpdate() {
        when(availabilitySlotRepository.releaseReservedSlot(SLOT_ID)).thenReturn(1);

        assertThatCode(() -> releaseService.releaseReservedSlot(SLOT_ID)).doesNotThrowAnyException();

        verify(availabilitySlotRepository).releaseReservedSlot(SLOT_ID);
        verifyNoMoreInteractions(availabilitySlotRepository);
    }

    @Test
    void rejectsAvailableSlotWhenConditionalUpdateChangesNoRows() {
        assertRejectedRelease();
    }

    @Test
    void rejectsBlockedSlotWhenConditionalUpdateChangesNoRows() {
        assertRejectedRelease();
    }

    @Test
    void rejectsMissingSlotWhenConditionalUpdateChangesNoRows() {
        assertRejectedRelease();
    }

    private void assertRejectedRelease() {
        when(availabilitySlotRepository.releaseReservedSlot(SLOT_ID)).thenReturn(0);

        assertThatThrownBy(() -> releaseService.releaseReservedSlot(SLOT_ID))
                .isInstanceOf(SlotReleaseRejectedException.class)
                .hasMessageContaining(SLOT_ID.toString());

        verify(availabilitySlotRepository).releaseReservedSlot(SLOT_ID);
        verifyNoMoreInteractions(availabilitySlotRepository);
    }
}
