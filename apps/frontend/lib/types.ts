export type ApiError = { errorCode?: string; message?: string };

export type AvailabilitySlot = {
  id: string;
  slotId: string;
  slotDate: string;
  startTime: string;
  endTime: string;
  status: string;
  usable: boolean;
  professionalId: string;
  professionalName: string;
  specialtyId: string;
  specialtyName: string;
};

export type Appointment = {
  id: string;
  patientId: string;
  professionalId: string;
  slotId: string;
  appointmentStatus: string;
  flowStage: string;
  reason: string;
  createdAt: string;
};

export type PatientAppointmentSummary = {
  appointmentId: string;
  status: string;
  flowStage: string | null;
  reason: string | null;
  specialtyName: string;
  professionalName: string;
  appointmentDate: string;
  startTime: string;
  endTime: string;
};
