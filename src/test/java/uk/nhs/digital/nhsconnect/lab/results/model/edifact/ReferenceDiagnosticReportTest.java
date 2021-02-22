package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ReferenceDiagnosticReportTest {

    private static final String VALID_EDIFACT = "RFF+SRI:13/CH001137K/211010191093";

    @Test
    void testFromStringWithValidInput() {
        ReferenceDiagnosticReport referenceDiagnosticReport = ReferenceDiagnosticReport.fromString(VALID_EDIFACT);

        assertEquals("13/CH001137K/211010191093", referenceDiagnosticReport.getReferenceNumber());
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
}
