package uk.nhs.digital.nhsconnect.lab.results.model.edifact.segmentgroup;

import org.junit.jupiter.api.Test;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.ClinicalInformationFreeText;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PatientClinicalInfoTest {
    @Test
    void testIndicator() {
        assertThat(PatientClinicalInfo.INDICATOR).isEqualTo("S10");
    }

    @Test
    void testGetClinicalInformationFreeTexts() {
        final var patientInfo = new PatientClinicalInfo(List.of(
            "ignore me",
            "FTX+CID+++TIRED ALL THE TIME, LOW Hb",
            "ignore me",
            "FTX+CID+++PAINS HANDS AND FEET.",
            "ignore me"
        ));
        assertThat(patientInfo.getClinicalInformationFreeTexts())
            .hasSize(2)
            .extracting(ClinicalInformationFreeText::getValue)
            .contains("CID+++TIRED ALL THE TIME, LOW Hb", "CID+++PAINS HANDS AND FEET.");
    }

    @Test
    void testLazyGettersWhenMissing() {
        final var patientInfo = new PatientClinicalInfo(List.of());
        assertThat(patientInfo.getClinicalInformationFreeTexts()).isEmpty();
    }
}
