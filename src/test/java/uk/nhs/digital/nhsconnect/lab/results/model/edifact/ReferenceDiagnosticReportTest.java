package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;

public class ReferenceDiagnosticReportTest {

    @Test
    void testMappingToEdifact() {
        var expectedReference = "RFF+SRI:13/CH001137K/211010191093";

        var referenceDiagnosticReport = ReferenceDiagnosticReport.builder()
            .referenceNumber("13/CH001137K/211010191093")
            .build();

        assertThat(referenceDiagnosticReport.getValue()).isEqualTo(expectedReference);
    }

    @Test
    void testMappingToEdifactWithEmptyValue() {
        var referenceDiagnosticReport = ReferenceDiagnosticReport.builder()
            .referenceNumber("")
            .build();

        assertThatThrownBy(referenceDiagnosticReport::toEdifact).isExactlyInstanceOf(EdifactValidationException.class);
    }

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
