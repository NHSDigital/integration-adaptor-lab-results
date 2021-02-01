package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;

class DiagnosticReportCodeTest {

    @Test
    void testMappingToEdifact() {
        var expectedValue = "GIS+N'";

        var diagnosticReportCode = DiagnosticReportCode.builder()
            .code("N")
            .build();

        assertEquals(expectedValue, diagnosticReportCode.toEdifact());
    }

    @Test
    void testThatMappingToEdifactWithEmptyTypeThrowsEdifactValidationException() {
        var diagnosticReportCode = DiagnosticReportCode.builder()
            .code("")
            .build();

        final EdifactValidationException exception = assertThrows(EdifactValidationException.class,
            () -> diagnosticReportCode.toEdifact());

        assertEquals("GIS: Diagnostic Report Code is required", exception.getMessage());
    }

    @Test
    void testBuildWithNullCodeThrowsException() {
        final NullPointerException exception =
            assertThrows(NullPointerException.class, () -> DiagnosticReportCode.builder().build());

        assertEquals("code is marked non-null but is null", exception.getMessage());

    }

    @Test
    void testFromStringWithValidInput() {
        DiagnosticReportCode diagnosticReportCode = DiagnosticReportCode.fromString("GIS+N");

        String actual = diagnosticReportCode.toEdifact();

        assertEquals("GIS+N'", actual);
    }

    @Test
    void testFromStringWithInvalidInput() {
        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> DiagnosticReportCode.fromString("wrong value"));

        assertEquals("Can't create DiagnosticReportCode from wrong value", exception.getMessage());
    }
}
