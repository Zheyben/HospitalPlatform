package com.hospital.platform.appointments.exception;

import com.hospital.platform.appointments.entity.AppointmentStatus;
import java.util.UUID;

public class InvalidAppointmentTransitionException extends RuntimeException {

    public InvalidAppointmentTransitionException(
            UUID appointmentId,
            AppointmentStatus currentStatus,
            String operation
    ) {
        super("Appointment " + appointmentId + " cannot be " + operation + " from " + currentStatus);
    }
}
