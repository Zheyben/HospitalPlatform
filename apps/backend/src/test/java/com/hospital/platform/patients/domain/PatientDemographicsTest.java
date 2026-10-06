package com.hospital.platform.patients.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class PatientDemographicsTest {

    @Test
    void normalizesOptionalValuesAndAllowsAffiliationOnlyForSupportedInsurance() {
        PatientDemographics demographics = new PatientDemographics("  Soltera  ", "  ", " Lima ",
                null, " 123-45 ", null, " Hermana ", "+51987654321");

        assertThat(demographics.maritalStatus()).isEqualTo("Soltera");
        assertThat(demographics.occupation()).isNull();
        assertThat(demographics.district()).isEqualTo("Lima");
        assertThat(demographics.affiliationNumber()).isEqualTo("123-45");
        demographics.requireValidAffiliation("SIS");
        demographics.requireValidAffiliation("EsSalud");
        assertThatThrownBy(() -> demographics.requireValidAffiliation("Particular"))
                .isInstanceOf(InvalidPatientDemographicsException.class);
    }
}
