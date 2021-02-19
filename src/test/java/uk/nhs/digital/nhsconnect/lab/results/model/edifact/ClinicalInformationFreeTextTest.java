package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import org.junit.jupiter.api.Test;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class ClinicalInformationFreeTextTest {
    @Test
    void testGetKey() {
        final var clinicalInfo = new ClinicalInformationFreeText();
        assertThat(clinicalInfo.getKey()).isEqualTo(ClinicalInformationFreeText.KEY);
    }

    @Test
    void testGetValue() {
        final var clinicalInfo = new ClinicalInformationFreeText("Comment");
        assertThat(clinicalInfo.getValue()).isEqualTo("CID+++Comment");
    }

    @Test
    void testFromStringWrongKey() {
        assertThatThrownBy(() -> ClinicalInformationFreeText.fromString("WRONG+++Comment"))
            .isExactlyInstanceOf(IllegalArgumentException.class)
            .hasMessage("Can't create ClinicalInformationFreeText (FTX+CID) from WRONG+++Comment");
    }

    @Test
    void testFromStringAllValues() {
        final var result = ClinicalInformationFreeText.fromString("FTX+CID+++Comment");
        assertThat(result.getTexts()).containsSequence("Comment");
    }

    @Test
    void testValidationPasses() {
        final var result = ClinicalInformationFreeText.fromString("FTX+CID+++Comment");
        assertAll(
            () -> assertDoesNotThrow(result::validateStateful),
            () -> assertDoesNotThrow(result::preValidate)
        );
    }

    @Test
    void testValidationMissingComment() {
        final var result = ClinicalInformationFreeText.fromString("FTX+CID+++");
        assertAll(
            () -> assertDoesNotThrow(result::validateStateful),
            () -> assertThatThrownBy(result::preValidate)
                .isExactlyInstanceOf(EdifactValidationException.class)
                .hasMessage("FTX+CID: At least one free text must be given.")
        );
    }
}
