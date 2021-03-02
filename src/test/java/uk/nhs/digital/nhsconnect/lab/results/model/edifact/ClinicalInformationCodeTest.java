package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import org.junit.jupiter.api.Test;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ClinicalInformationCodeTest {
    @Test
    void testThatMappingToEdifactWithEmptyTypeThrowsEdifactValidationException() {
        var edifactString = "CIN+";

        assertThatThrownBy(() -> ClinicalInformationCode.fromString(edifactString).validate())
                .isInstanceOf(EdifactValidationException.class)
                .hasMessage("CIN: Clinical Information Code is required");
    }

    @Test
    void testFromStringWithValidInput() {
        var edifactString = "CIN+UN";

        var clinicalInformationCode = ClinicalInformationCode.builder()
                .code("UN")
                .build();

        assertThat(ClinicalInformationCode.fromString(edifactString))
                .usingRecursiveComparison()
                .isEqualTo(clinicalInformationCode);
    }

    @Test
    void testFromStringWithInvalidInput() {
        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> ClinicalInformationCode.fromString("wrong value"));

        assertEquals("Can't create ClinicalInformationCode from wrong value", exception.getMessage());
    }
}
