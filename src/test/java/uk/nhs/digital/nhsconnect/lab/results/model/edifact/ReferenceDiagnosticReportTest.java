package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import org.junit.jupiter.api.Test;

public class ReferenceDiagnosticReportTest {

    @Test
    void testFromStringWithValidInput() {
        ReferenceDiagnosticReport referenceDiagnosticReport = ReferenceDiagnosticReport.fromString("RFF+SRI:15/CH000037K/200010191704");
        ReferenceDiagnosticReport expectedReferenceDiagnosticReport = new ReferenceDiagnosticReport("15/CH000037K/200010191704");

        assertThat(referenceDiagnosticReport.getValue()).isEqualTo(expectedReferenceDiagnosticReport.getValue());
    }


    @Test
    void testFromStringWithInvalidInput() {
        assertThatThrownBy(() -> ReferenceDiagnosticReport.fromString("RFF+TNN:15/CH000037K/200010191704"))
            .isExactlyInstanceOf(IllegalArgumentException.class);
    }

}
