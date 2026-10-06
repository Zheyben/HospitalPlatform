package com.hospital.platform.patients.domain;

public record PatientDemographics(
        String maritalStatus, String occupation, String district, String educationLevel,
        String affiliationNumber, String emergencyContactName,
        String emergencyContactRelationship, String emergencyContactPhone
) {
    public PatientDemographics {
        maritalStatus = normalize(maritalStatus);
        occupation = normalize(occupation);
        district = normalize(district);
        educationLevel = normalize(educationLevel);
        affiliationNumber = normalize(affiliationNumber);
        emergencyContactName = normalize(emergencyContactName);
        emergencyContactRelationship = normalize(emergencyContactRelationship);
        emergencyContactPhone = normalize(emergencyContactPhone);
    }

    public static PatientDemographics empty() {
        return new PatientDemographics(null, null, null, null, null, null, null, null);
    }

    public void requireValidAffiliation(String insuranceCode) {
        if (affiliationNumber != null && !"SIS".equalsIgnoreCase(insuranceCode)
                && !"ESSALUD".equalsIgnoreCase(insuranceCode)) {
            throw new InvalidPatientDemographicsException("Affiliation number requires SIS or EsSalud");
        }
    }

    private static String normalize(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }
}
