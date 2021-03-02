package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DiagnosticReportCodeTest {

    @Test
    void testBuildWithNullCodeThrowsException() {
        final NullPointerException exception =
                assertThrows(NullPointerException.class, () -> DiagnosticReportCode.builder().build());

        assertEquals("code is marked non-null but is null", exception.getMessage());
    }

    @Test
    void testFromStringWithValidInput() {
        DiagnosticReportCode diagnosticReportCode = DiagnosticReportCode.fromString("GIS+N");

        assertEquals("N", diagnosticReportCode.getCode());
    }

    @Test
    void testFromStringWithInvalidInput() {
        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> DiagnosticReportCode.fromString("wrong value"));

        assertEquals("Can't create DiagnosticReportCode from wrong value", exception.getMessage());
    }
}
