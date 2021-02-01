package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DiagnosticReportDateIssuedTest {

    private static final LocalDateTime FIXED_TIME = LocalDateTime.of(2020, 01, 28, 9, 57);
    private static final String VALID_EDIFACT = "DTM+ISR:202001280957:203'";
    private static final String VALID_EDIFACT_VALUE = "ISR:202001280957:203";

    @Test
    void testToEdifactForValidDiagnosticReportIssued() {
        final DiagnosticReportDateIssued diagnosticReportDateIssued = DiagnosticReportDateIssued.builder()
            .dateIssued(FIXED_TIME)
            .build();

        final String actual = diagnosticReportDateIssued.toEdifact();

        assertEquals(VALID_EDIFACT, actual);
    }

    @Test
    void testBuildWithEmptyTimestampThrowsException() {
        final NullPointerException exception =
            assertThrows(NullPointerException.class, () -> DiagnosticReportDateIssued.builder().build());

        assertEquals("dateIssued is marked non-null but is null", exception.getMessage());
    }

    @Test
    void testFromStringWithValidEdifactStringReturnsDiagnosticReportDateIssued() {
        final DiagnosticReportDateIssued diagnosticReportDateIssued = DiagnosticReportDateIssued.fromString(VALID_EDIFACT);

        assertEquals("DTM", diagnosticReportDateIssued.getKey());
        assertEquals(VALID_EDIFACT_VALUE, diagnosticReportDateIssued.getValue());
        assertEquals(FIXED_TIME, diagnosticReportDateIssued.getDateIssued());
    }

    @Test
    void testFromStringWithInvalidEdifactStringThrowsException() {
        final IllegalArgumentException exception =
            assertThrows(IllegalArgumentException.class, () -> DiagnosticReportDateIssued.fromString("wrong value"));

        assertEquals("Can't create DiagnosticReportDateIssued from wrong value", exception.getMessage());
    }

}
