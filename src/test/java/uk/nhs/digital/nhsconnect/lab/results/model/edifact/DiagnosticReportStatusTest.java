package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DiagnosticReportStatusTest {

    private static final String VALID_EDIFACT = "STS+Details+UN";
    private static final String KEY = "STS";

    @Test
    void testBuildWithNullEventThrowsException() {
        final NullPointerException exception =
            assertThrows(NullPointerException.class, () -> DiagnosticReportStatus.builder().build());

        assertEquals("event is marked non-null but is null", exception.getMessage());
    }

    @Test
    void testFromStringWithDetailsInDiagnosticReportStatusReturnsDiagnosticReportStatus() {
        final DiagnosticReportStatus diagnosticReportStatus = DiagnosticReportStatus.fromString(VALID_EDIFACT);

        assertAll(
            () -> assertEquals(KEY, diagnosticReportStatus.getKey()),
            () -> assertEquals(ReportStatusCode.UNSPECIFIED, diagnosticReportStatus.getEvent()),
            () -> assertEquals("Details", diagnosticReportStatus.getDetail())
        );
    }

    @Test
    void testFromStringWithInvalidEdifactStringThrowsException() {
        final IllegalArgumentException exception =
            assertThrows(IllegalArgumentException.class, () -> DiagnosticReportStatus.fromString("wrong value"));

        assertEquals("Can't create DiagnosticReportStatus from wrong value", exception.getMessage());
    }
}
