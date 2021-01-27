package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;

public class DiagnosticReportCodeTest {

    @Test
    void testMappingToEdifact() {
        var expectedValue = "GIS+N'";

        var diagnosticReportCode = DiagnosticReportCode.builder()
            .code("N")
            .build();

        assertThat(diagnosticReportCode.toEdifact()).isEqualTo(expectedValue);
    }

    @Test
    void testThatMappingToEdifactWithEmptyTypeThrowsEdifactValidationException() {
        var diagnosticReportCode = DiagnosticReportCode.builder()
            .code("")
            .build();

        assertThatThrownBy(diagnosticReportCode::toEdifact).isInstanceOf(EdifactValidationException.class);
    }

}
