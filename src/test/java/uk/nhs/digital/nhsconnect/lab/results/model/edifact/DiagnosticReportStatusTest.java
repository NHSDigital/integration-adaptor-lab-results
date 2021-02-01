package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class DiagnosticReportStatusTest {

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
        final NullPointerException exception =
            assertThrows(NullPointerException.class, () -> DiagnosticReportStatus.builder().build());

        assertEquals("event is marked non-null but is null", exception.getMessage());
    }

    @Test
    void testFromStringWithValidEdifactStringReturnsDiagnosticReportStatus() {
        final DiagnosticReportStatus diagnosticReportStatus = DiagnosticReportStatus.fromString(VALID_EDIFACT);

        assertAll(
            () -> assertEquals("STS", diagnosticReportStatus.getKey()),
            () -> assertEquals(VALID_EDIFACT_VALUE, diagnosticReportStatus.getValue()),
            () -> assertEquals("STS++UN'", diagnosticReportStatus.toEdifact())

        );
    }

    @Test
    void testFromStringWithDetailsInDiagnosticReportStatusReturnsDiagnosticReportStatus() {
        final DiagnosticReportStatus diagnosticReportStatus = DiagnosticReportStatus.fromString("STS+Details+UN");

        assertAll(
            () -> assertEquals("STS", diagnosticReportStatus.getKey()),
            () -> assertEquals("Details+UN", diagnosticReportStatus.getValue()),
            () -> assertEquals(ReportStatusCode.UNSPECIFIED, diagnosticReportStatus.getEvent()),
            () -> assertEquals("Details", diagnosticReportStatus.getDetail()),
            () -> assertEquals("STS+Details+UN'", diagnosticReportStatus.toEdifact())
        );
    }

    @Test
    void testFromStringWithInvalidEdifactStringThrowsException() {
        final IllegalArgumentException exception =
            assertThrows(IllegalArgumentException.class, () -> DiagnosticReportStatus.fromString("wrong value"));

        assertEquals("Can't create DiagnosticReportStatus from wrong value", exception.getMessage());
    }
}
