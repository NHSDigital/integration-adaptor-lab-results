package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;

public class ReferenceDiagnosticReportTest {

    private static final String VALID_EDIFACT = "RFF+SRI:13/CH001137K/211010191093";
    private static final String VALID_EDIFACT_VALUE = "SRI:13/CH001137K/211010191093";

    @Test
    void testFromStringWithValidInput() {
        ReferenceDiagnosticReport referenceDiagnosticReport = ReferenceDiagnosticReport.fromString(VALID_EDIFACT);

        assertEquals(VALID_EDIFACT_VALUE, referenceDiagnosticReport.getValue());
    }

    @Test
    void testFromStringWithInvalidInput() {
        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> ReferenceDiagnosticReport.fromString("RFF+ABC:13/CH001137K/211010191093"));

        assertEquals("Can't create ReferenceDiagnosticReport from RFF+ABC:13/CH001137K/211010191093",
            exception.getMessage());
    }

    @Test
    void testBuildWithNullReferenceThrowsException() {
        assertThrows(NullPointerException.class, () -> ReferenceDiagnosticReport.builder().build());
    }

    @Test
    void testToEdifactWithValidInput() {
        final String expected = "RFF+SRI:13/CH001137K/211010191093'";

        ReferenceDiagnosticReport referenceDiagnosticReport = ReferenceDiagnosticReport.builder()
            .referenceNumber("13/CH001137K/211010191093")
            .build();

        String actual = referenceDiagnosticReport.toEdifact();

        assertEquals(expected, actual);
    }

    @Test
    void testToEdifactWithInvalidInput() {
        ReferenceDiagnosticReport referenceDiagnosticReport = ReferenceDiagnosticReport.builder()
            .referenceNumber("")
            .build();

        final EdifactValidationException exception = assertThrows(EdifactValidationException.class,
            referenceDiagnosticReport::toEdifact);

        assertEquals("RFF: Diagnostic Report Reference is required", exception.getMessage());
    }

}
