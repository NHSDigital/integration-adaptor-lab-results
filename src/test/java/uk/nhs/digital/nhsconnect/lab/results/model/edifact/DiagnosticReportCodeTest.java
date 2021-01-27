package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import org.junit.jupiter.api.Test;

public class DiagnosticReportCodeTest {

    @Test
    void when_MappingToEdifact_Then_ReturnCorrectString() {
        var expectedValue = "GIS+N'";

        var diagnosticReportCode = DiagnosticReportCode.builder()
            .code("N")
            .build();

        assertThat(diagnosticReportCode.toEdifact()).isEqualTo(expectedValue);
    }
}
