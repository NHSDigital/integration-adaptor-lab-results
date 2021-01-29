package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;

public class DiagnosticReportStatusTest {

    private static final String VALID_EDIFACT = "STS++UN";
    private static final String VALID_EDIFACT_VALUE = "UN";


    @Test
    void testToEdifactWithValidDiagnosticReportStatus() {
        final String expected = "STS++UN'";

        final DiagnosticReportStatus diagnosticReportStatus = DiagnosticReportStatus.builder()
            .event("+UN")
            .build();

        final String actualValue = diagnosticReportStatus.toEdifact();

        assertEquals(expected, actualValue);
    }

    @Test
    void testToEdifactWithInvalidDiagnosticReportStatus() {
        final DiagnosticReportStatus diagnosticReportStatus = DiagnosticReportStatus.builder()
            .event("")
            .build();

        final EdifactValidationException exception = assertThrows(EdifactValidationException.class,
            diagnosticReportStatus::toEdifact);

        assertEquals("STS: Status is required", exception.getMessage());
    }

    @Test
    void testFromStringWithValidEdifactStringReturnsDiagnosticReportStatus() {
        final DiagnosticReportStatus diagnosticReportStatus = DiagnosticReportStatus.fromString(VALID_EDIFACT);

        assertEquals("STS", diagnosticReportStatus.getKey());
        assertEquals(VALID_EDIFACT_VALUE, diagnosticReportStatus.getValue());
    }

    @Test
    void testFromStringWithInvalidEdifactStringThrowsException() {
        final IllegalArgumentException exception =
            assertThrows(IllegalArgumentException.class, () -> DiagnosticReportStatus.fromString("wrong value"));

        assertEquals("Can't create DiagnosticReportStatus from wrong value", exception.getMessage());
    }
}
