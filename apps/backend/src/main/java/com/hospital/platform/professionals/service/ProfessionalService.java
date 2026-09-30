package com.hospital.platform.professionals.service;

import com.hospital.platform.professionals.dto.CreateProfessionalRequestDTO;
import com.hospital.platform.professionals.dto.ProfessionalResponseDTO;
import com.hospital.platform.professionals.dto.UpdateProfessionalRequestDTO;
import com.hospital.platform.professionals.entity.Professional;
import com.hospital.platform.professionals.exception.DuplicateProfessionalException;
import com.hospital.platform.professionals.exception.ProfessionalNotFoundException;
import com.hospital.platform.professionals.mapper.ProfessionalMapper;
import com.hospital.platform.professionals.repository.ProfessionalRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfessionalService {

    private final ProfessionalRepository professionalRepository;
    private final ProfessionalMapper professionalMapper;

    public ProfessionalService(ProfessionalRepository professionalRepository) {
        this.professionalRepository = professionalRepository;
        this.professionalMapper = new ProfessionalMapper();
    }

    @Transactional
    public ProfessionalResponseDTO createProfessional(CreateProfessionalRequestDTO request) {
        String licenseNumber = normalizeLicenseNumber(request.licenseNumber());
        assertLicenseAvailable(licenseNumber);

        Professional professional = new Professional(null, request.userId(), licenseNumber);

        return professionalMapper.toResponse(professionalRepository.save(professional));
    }

    @Transactional(readOnly = true)
    public List<ProfessionalResponseDTO> findProfessionals() {
        return professionalMapper.toResponseList(professionalRepository.findAllByDeletedAtIsNullOrderByCreatedAtDesc());
    }

    @Transactional(readOnly = true)
    public ProfessionalResponseDTO findProfessionalById(UUID professionalId) {
        return professionalMapper.toResponse(findActiveProfessional(professionalId));
    }

    @Transactional
    public ProfessionalResponseDTO updateProfessional(UUID professionalId, UpdateProfessionalRequestDTO request) {
        Professional professional = findActiveProfessional(professionalId);
        String licenseNumber = normalizeLicenseNumber(request.licenseNumber());

        if (!professional.getLicenseNumber().equalsIgnoreCase(licenseNumber)
                && professionalRepository.existsByLicenseNumberIgnoreCaseAndIdNot(licenseNumber, professionalId)) {
            throw new DuplicateProfessionalException(licenseNumber);
        }

        professional.updateBasicInfo(licenseNumber);
        return professionalMapper.toResponse(professional);
    }

    @Transactional
    public ProfessionalResponseDTO deactivateProfessional(UUID professionalId) {
        Professional professional = findActiveProfessional(professionalId);

        professional.deactivate();
        return professionalMapper.toResponse(professional);
    }

    private Professional findActiveProfessional(UUID professionalId) {
        return professionalRepository.findByIdAndDeletedAtIsNull(professionalId)
                .orElseThrow(() -> new ProfessionalNotFoundException(professionalId));
    }

    private void assertLicenseAvailable(String licenseNumber) {
        if (professionalRepository.existsByLicenseNumberIgnoreCase(licenseNumber)) {
            throw new DuplicateProfessionalException(licenseNumber);
        }
    }

    private String normalizeLicenseNumber(String licenseNumber) {
        return licenseNumber.trim();
    }
}
