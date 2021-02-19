package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import org.junit.jupiter.api.Test;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ClinicalInformationCodeTest {

    @Test
    void testMappingToEdifact() {
        var expectedValue = "CIN+UN'";

        var clinicalInformationCode = ClinicalInformationCode.builder()
            .code("UN")
            .build();

        assertEquals(expectedValue, clinicalInformationCode.toEdifact());
    }

    @Test
    void testThatMappingToEdifactWithEmptyTypeThrowsEdifactValidationException() {
        var clinicalInformationCode = ClinicalInformationCode.builder()
            .code("")
            .build();

        final EdifactValidationException exception = assertThrows(EdifactValidationException.class,
            clinicalInformationCode::toEdifact);

        assertEquals("CIN: Clinical Information Code is required", exception.getMessage());
    }

    @Test
    void testBuildWithNullCodeThrowsException() {
        final NullPointerException exception =
            assertThrows(NullPointerException.class, () -> ClinicalInformationCode.builder().build());

        assertEquals("code is marked non-null but is null", exception.getMessage());

    }

    @Test
    void testFromStringWithValidInput() {
        ClinicalInformationCode clinicalInformationCode = ClinicalInformationCode.fromString("CIN+UN");

        String actual = clinicalInformationCode.toEdifact();

        assertEquals("CIN+UN'", actual);
    }

    @Test
    void testFromStringWithInvalidInput() {
        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> ClinicalInformationCode.fromString("wrong value"));

        assertEquals("Can't create ClinicalInformationCode from wrong value", exception.getMessage());
    }
}
