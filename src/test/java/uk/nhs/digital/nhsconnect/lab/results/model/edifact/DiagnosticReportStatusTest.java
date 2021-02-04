package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class DiagnosticReportStatusTest {

    private static final String VALID_EDIFACT_WITHOUT_DETAILS = "STS++UN";
    private static final String EXPECTED_EDIFACT_STRING_WITHOUT_DETAILS = "STS++UN'";
    private static final String VALID_EDIFACT_VALUE_WITHOUT_DETAILS = "UN";

    private static final String VALID_EDIFACT = "STS+Details+UN";
    private static final String EXPECTED_EDIFACT_STRING = "STS+Details+UN'";
    private static final String VALID_EDIFACT_VALUE = "Details+UN";

    private static final String KEY = "STS";

    @Test
    void testToEdifactWithValidDiagnosticReportStatus() {
        final DiagnosticReportStatus diagnosticReportStatus = DiagnosticReportStatus.builder()
            .event(ReportStatusCode.UNSPECIFIED)
            .build();

        final String actualValue = diagnosticReportStatus.toEdifact();

        assertEquals(EXPECTED_EDIFACT_STRING_WITHOUT_DETAILS, actualValue);
    }

    @Test
    void testToEdifactWithDetailsInDiagnosticReportStatus() {
        final DiagnosticReportStatus diagnosticReportStatus = DiagnosticReportStatus.builder()
            .detail("Details")
            .event(ReportStatusCode.UNSPECIFIED)
            .build();

        final String actualValue = diagnosticReportStatus.toEdifact();

        assertEquals(EXPECTED_EDIFACT_STRING, actualValue);
    }

    @Test
    void testBuildWithNullEventThrowsException() {
        final NullPointerException exception =
            assertThrows(NullPointerException.class, () -> DiagnosticReportStatus.builder().build());

        assertEquals("event is marked non-null but is null", exception.getMessage());
    }

    @Test
    void testFromStringWithValidEdifactStringReturnsDiagnosticReportStatus() {
        final DiagnosticReportStatus diagnosticReportStatus = DiagnosticReportStatus.fromString(VALID_EDIFACT_WITHOUT_DETAILS);

        assertAll(
            () -> assertEquals(KEY, diagnosticReportStatus.getKey()),
            () -> assertEquals(VALID_EDIFACT_VALUE_WITHOUT_DETAILS, diagnosticReportStatus.getValue()),
            () -> assertEquals(EXPECTED_EDIFACT_STRING_WITHOUT_DETAILS, diagnosticReportStatus.toEdifact())

        );
    }

    @Test
    void testFromStringWithDetailsInDiagnosticReportStatusReturnsDiagnosticReportStatus() {
        final DiagnosticReportStatus diagnosticReportStatus = DiagnosticReportStatus.fromString(VALID_EDIFACT);

        assertAll(
            () -> assertEquals(KEY, diagnosticReportStatus.getKey()),
            () -> assertEquals(VALID_EDIFACT_VALUE, diagnosticReportStatus.getValue()),
            () -> assertEquals(ReportStatusCode.UNSPECIFIED, diagnosticReportStatus.getEvent()),
            () -> assertEquals("Details", diagnosticReportStatus.getDetail()),
            () -> assertEquals(EXPECTED_EDIFACT_STRING, diagnosticReportStatus.toEdifact())
        );
    }

    @Test
    void testFromStringWithInvalidEdifactStringThrowsException() {
        final IllegalArgumentException exception =
            assertThrows(IllegalArgumentException.class, () -> DiagnosticReportStatus.fromString("wrong value"));

        assertEquals("Can't create DiagnosticReportStatus from wrong value", exception.getMessage());
    }
}
