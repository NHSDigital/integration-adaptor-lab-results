package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import org.junit.jupiter.api.Test;
import uk.nhs.digital.nhsconnect.lab.results.model.enums.DateFormat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DiagnosticReportDateIssuedTest {

    private static final String VALID_EDIFACT = "DTM+ISR:202001280957:203";

    @Test
    void testBuildWithEmptyTimestampThrowsException() {
        final NullPointerException exception =
            assertThrows(NullPointerException.class, () -> DiagnosticReportDateIssued.builder().build());

        assertEquals("dateIssued is marked non-null but is null", exception.getMessage());
    }

    @Test
    void testFromStringWithValidEdifactStringReturnsDiagnosticReportDateIssued() {
        final var diagnosticReportDateIssued = DiagnosticReportDateIssued.fromString(VALID_EDIFACT);

        assertEquals("DTM", diagnosticReportDateIssued.getKey());
        assertEquals("202001280957", diagnosticReportDateIssued.getDateIssued());
        assertEquals(DateFormat.CCYYMMDDHHMM, diagnosticReportDateIssued.getDateFormat());
    }

    @Test
    void testFromStringWithInvalidEdifactStringThrowsException() {
        final IllegalArgumentException exception =
            assertThrows(IllegalArgumentException.class, () -> DiagnosticReportDateIssued.fromString("wrong value"));

        assertEquals("Can't create DiagnosticReportDateIssued from wrong value", exception.getMessage());
    }

}
