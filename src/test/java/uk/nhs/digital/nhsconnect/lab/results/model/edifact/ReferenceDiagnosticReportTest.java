package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import org.junit.jupiter.api.Test;

public class ReferenceDiagnosticReportTest {

    @Test
    void testFromStringWithValidInput() {
        ReferenceDiagnosticReport referenceDiagnosticReport = ReferenceDiagnosticReport.fromString("RFF+SRI:13/CH001137K/211010191093");
        ReferenceDiagnosticReport expectedReferenceDiagnosticReport = new ReferenceDiagnosticReport("13/CH001137K/211010191093");

        assertThat(referenceDiagnosticReport.getValue()).isEqualTo(expectedReferenceDiagnosticReport.getValue());
    }


    @Test
    void testFromStringWithInvalidInput() {
        assertThatThrownBy(() -> ReferenceDiagnosticReport.fromString("RFF+ABC:13/CH001137K/211010191093"))
            .isExactlyInstanceOf(IllegalArgumentException.class);
    }

}
