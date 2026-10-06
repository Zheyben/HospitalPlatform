package com.hospital.platform.professionals.service;

import com.hospital.platform.agenda.contract.CapacityGateway;
import com.hospital.platform.professionals.dto.CreateProfessionalRequestDTO;
import com.hospital.platform.professionals.dto.ProfessionalResponseDTO;
import com.hospital.platform.professionals.dto.UpdateProfessionalRequestDTO;
import com.hospital.platform.professionals.entity.Professional;
import com.hospital.platform.professionals.exception.DuplicateProfessionalException;
import com.hospital.platform.professionals.exception.InvalidProfessionalLicenseException;
import com.hospital.platform.professionals.exception.ProfessionalDomainConflictException;
import com.hospital.platform.professionals.exception.ProfessionalNotFoundException;
import com.hospital.platform.professionals.exception.SpecialtyNotAvailableException;
import com.hospital.platform.professionals.repository.ProfessionalManagementRepository;
import com.hospital.platform.professionals.repository.ProfessionalRepository;
import com.hospital.platform.users.entity.User;
import com.hospital.platform.users.repository.UserRepository;
import com.hospital.platform.users.service.UserService;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfessionalService {

    private final ProfessionalRepository professionals;
    private final ProfessionalManagementRepository management;
    private final UserRepository users;
    private final UserService userService;
    private final CapacityGateway capacityGateway;

    public ProfessionalService(ProfessionalRepository professionals, ProfessionalManagementRepository management,
                               UserRepository users, UserService userService, CapacityGateway capacityGateway) {
        this.professionals = professionals;
        this.management = management;
        this.users = users;
        this.userService = userService;
        this.capacityGateway = capacityGateway;
    }

    @Transactional
    public ProfessionalResponseDTO createProfessional(CreateProfessionalRequestDTO request) {
        String license = normalizeLicenseNumber(request.licenseNumber());
        if (professionals.existsByLicenseNumberIgnoreCase(license)) {
            throw new DuplicateProfessionalException(license);
        }
        assertActiveSpecialty(request.specialtyId());

        User user = userService.registerProfessionalUser(request.email(), request.password(),
                requiredName(request.firstName()), requiredName(request.lastName()));
        Professional professional = professionals.saveAndFlush(new Professional(null, user.getId(), license));
        management.assignSpecialty(professional.getId(), request.specialtyId());
        return response(professional.getId());
    }

    @Transactional(readOnly = true)
    public List<ProfessionalResponseDTO> findProfessionals() {
        return management.findAll();
    }

    @Transactional(readOnly = true)
    public ProfessionalResponseDTO findProfessionalById(UUID professionalId) {
        return response(professionalId);
    }

    @Transactional
    public ProfessionalResponseDTO updateProfessional(UUID professionalId, UpdateProfessionalRequestDTO request) {
        Professional existing = activeProfessional(professionalId);
        if (existing.getUserId() == null) {
            throw new ProfessionalDomainConflictException("Professional has no linked account");
        }
        // Capacity mutations lock the user before the professional. Use that same order.
        capacityGateway.lockUser(existing.getUserId());
        Professional professional = activeProfessional(professionalId);
        User user = users.findActiveByIdWithRoles(professional.getUserId())
                .orElseThrow(() -> new ProfessionalDomainConflictException("Professional account is unavailable"));

        String license = normalizeLicenseNumber(request.licenseNumber());
        if (!professional.getLicenseNumber().equalsIgnoreCase(license)
                && professionals.existsByLicenseNumberIgnoreCaseAndIdNot(license, professionalId)) {
            throw new DuplicateProfessionalException(license);
        }
        assertActiveSpecialty(request.specialtyId());
        UUID currentSpecialty = management.assignedSpecialty(professionalId)
                .orElseThrow(() -> new ProfessionalDomainConflictException("Professional has no specialty"));
        if (!currentSpecialty.equals(request.specialtyId())) {
            if (management.hasSchedules(professionalId)) {
                throw new ProfessionalDomainConflictException(
                        "Cannot change specialty while schedules reference the current assignment");
            }
            management.replaceSpecialty(professionalId, request.specialtyId());
        }
        user.setNames(requiredName(request.firstName()), requiredName(request.lastName()));
        professional.updateBasicInfo(license);
        users.flush();
        professionals.flush();
        return response(professionalId);
    }

    @Transactional
    public ProfessionalResponseDTO changeStatus(UUID professionalId, boolean active) {
        if (!professionals.existsById(professionalId)) {
            throw new ProfessionalNotFoundException(professionalId);
        }
        if (active) {
            capacityGateway.reactivateProfessional(professionalId);
        } else {
            capacityGateway.deactivateProfessional(professionalId);
        }
        return response(professionalId);
    }

    @Transactional
    public ProfessionalResponseDTO deactivateProfessional(UUID professionalId) {
        return changeStatus(professionalId, false);
    }

    private Professional activeProfessional(UUID id) {
        return professionals.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ProfessionalNotFoundException(id));
    }

    private ProfessionalResponseDTO response(UUID id) {
        return management.findById(id).orElseThrow(() -> new ProfessionalNotFoundException(id));
    }

    private void assertActiveSpecialty(UUID id) {
        if (id == null || !management.isActiveSpecialty(id)) {
            throw new SpecialtyNotAvailableException();
        }
    }

    private String normalizeLicenseNumber(String value) {
        if (value == null || !value.trim().matches("[0-9]{4,6}")) {
            throw new InvalidProfessionalLicenseException();
        }
        return value.trim();
    }

    private String requiredName(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Professional name is required");
        }
        return value.trim();
    }
}
