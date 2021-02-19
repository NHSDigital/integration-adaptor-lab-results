package uk.nhs.digital.nhsconnect.lab.results.model.edifact.segmentgroup;

import org.junit.jupiter.api.Test;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.ClinicalInformationCode;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.FreeTextSegment;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.MissingSegmentException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

class PatientClinicalInfoTest {
    @Test
    void testIndicator() {
        assertThat(PatientClinicalInfo.INDICATOR).isEqualTo("S10");
    }

    @Test
    void testGetClinicalInformationCode() {
        final var patientInfo = new PatientClinicalInfo(List.of(
            "ignore me",
            "CIN+UN",
            "ignore me"
        ));
        assertThat(patientInfo.getClinicalInformationCode())
            .isNotNull()
            .extracting(ClinicalInformationCode::getValue)
            .isEqualTo("UN");
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
        assertThat(patientInfo.getFreeTexts())
            .hasSize(2)
            .extracting(FreeTextSegment::getValue)
            .contains("CID+++TIRED ALL THE TIME, LOW Hb", "CID+++PAINS HANDS AND FEET.");
    }

    @Test
    @SuppressWarnings("ResultOfMethodCallIgnored")
    void testLazyGettersWhenMissing() {
        final var patientInfo = new PatientClinicalInfo(List.of());
        assertAll(
            () -> assertThatThrownBy(patientInfo::getClinicalInformationCode)
                .isExactlyInstanceOf(MissingSegmentException.class)
                .hasMessage("EDIFACT section is missing segment CIN"),
            () -> assertThat(patientInfo.getFreeTexts()).isEmpty()
        );
    }
}
