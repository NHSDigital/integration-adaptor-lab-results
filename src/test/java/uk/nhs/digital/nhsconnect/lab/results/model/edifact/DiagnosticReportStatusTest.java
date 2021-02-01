package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class DiagnosticReportStatusTest {

    private static final String VALID_EDIFACT = "STS++UN";
    private static final String VALID_EDIFACT_VALUE = "UN";


    @Test
    void testToEdifactWithValidDiagnosticReportStatus() {
        final String expected = "STS++UN'";

        final DiagnosticReportStatus diagnosticReportStatus = DiagnosticReportStatus.builder()
            .event(ReportStatusCode.UNSPECIFIED)
            .build();

        final String actualValue = diagnosticReportStatus.toEdifact();

        assertEquals(expected, actualValue);
    }

    @Test
    void testToEdifactWithDetailsInDiagnosticReportStatus() {
        final String expected = "STS+Details+UN'";

        final DiagnosticReportStatus diagnosticReportStatus = DiagnosticReportStatus.builder()
            .detail("Details")
            .event(ReportStatusCode.UNSPECIFIED)
            .build();

        final String actualValue = diagnosticReportStatus.toEdifact();

        assertEquals(expected, actualValue);
    }

    @Test
    void testBuildWithNullEventThrowsException() {
        assertThrows(NullPointerException.class, () -> DiagnosticReportStatus.builder().build());
    }

    @Test
    void testFromStringWithValidEdifactStringReturnsDiagnosticReportStatus() {
        final DiagnosticReportStatus diagnosticReportStatus = DiagnosticReportStatus.fromString(VALID_EDIFACT);

        assertEquals("STS", diagnosticReportStatus.getKey());
        assertEquals(VALID_EDIFACT_VALUE, diagnosticReportStatus.getValue());
        assertEquals("STS++UN'", diagnosticReportStatus.toEdifact());
    }

    @Test
    void testFromStringWithInvalidEdifactStringThrowsException() {
        final IllegalArgumentException exception =
            assertThrows(IllegalArgumentException.class, () -> DiagnosticReportStatus.fromString("wrong value"));

        assertEquals("Can't create DiagnosticReportStatus from wrong value", exception.getMessage());
    }
}
