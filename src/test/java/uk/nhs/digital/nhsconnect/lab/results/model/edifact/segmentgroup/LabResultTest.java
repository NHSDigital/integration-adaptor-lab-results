package uk.nhs.digital.nhsconnect.lab.results.model.edifact.segmentgroup;

import org.junit.jupiter.api.Test;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.ComplexReferenceRangeFreeText;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.DiagnosticReportCode;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.InvestigationResultFreeText;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.LaboratoryInvestigation;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.LaboratoryInvestigationResult;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.SequenceDetails;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.SequenceReference;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.ServiceProviderCommentFreeText;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.TestStatus;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.MissingSegmentException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

class LabResultTest {
    @Test
    void testIndicator() {
        assertThat(LabResult.INDICATOR).isEqualTo("GIS");
    }

    @Test
    void testGetDiagnosticReportCode() {
        final var labResult = new LabResult(List.of(
            "ignore me",
            "GIS+N",
            "ignore me"
        ));
        assertThat(labResult.getDiagnosticReportCode())
            .isNotNull()
            .extracting(DiagnosticReportCode::getValue)
            .isEqualTo("N");
    }

    @Test
    void testGetLaboratoryInvestigation() {
        final var labResult = new LabResult(List.of(
            "ignore me",
            "INV+MQ+42R4.:911::Serum ferritin",
            "ignore me"
        ));
        assertThat(labResult.getLaboratoryInvestigation())
            .isNotNull()
            .extracting(LaboratoryInvestigation::getValue)
            .isEqualTo("MQ+42R4.:911::Serum ferritin");
    }

    @Test
    void testGetSequenceDetails() {
        final var labResult = new LabResult(List.of(
            "ignore me",
            "SEQ++ABC123",
            "ignore me"
        ));
        assertThat(labResult.getSequenceDetails())
            .isPresent()
            .map(SequenceDetails::getValue)
            .contains("ABC123");
    }

    @Test
    void testGetLaboratoryInvestigationResult() {
        final var labResult = new LabResult(List.of(
            "ignore me",
            "RSL+NV+11.9:7++:::ng/mL+HI",
            "ignore me"
        ));
        assertThat(labResult.getLaboratoryInvestigationResult())
            .isPresent()
            .map(LaboratoryInvestigationResult::getValue)
            .contains("NV+11.9:7++:::ng/mL+HI");
    }

    @Test
    void testGetTestStatus() {
        final var labResult = new LabResult(List.of(
            "ignore me",
            "STS++CO",
            "ignore me"
        ));
        assertThat(labResult.getTestStatus())
            .isPresent()
            .map(TestStatus::getValue)
            .contains("CO");
    }

    @Test
    void testGetServiceProviderCommentFreeTexts() {
        final var labResult = new LabResult(List.of(
            "ignore me",
            "FTX+SPC+++red blood cell seen",
            "ignore me",
            "FTX+SPC+++Note low platelets",
            "ignore me"
        ));
        assertThat(labResult.getServiceProviderCommentFreeTexts())
            .hasSize(2)
            .map(ServiceProviderCommentFreeText::getValue)
            .contains("SPC+++red blood cell seen", "SPC+++Note low platelets");
    }

    @Test
    void testGetInvestigationResultFreeTexts() {
        final var labResult = new LabResult(List.of(
            "ignore me",
            "FTX+RIT+++URINARY STONE weight 13 mg.",
            "ignore me",
            "FTX+RIT+++Consisted of 36% Calcium oxalate and 64% Calcium phosphate.",
            "ignore me"
        ));
        assertThat(labResult.getInvestigationResultFreeTexts())
            .hasSize(2)
            .map(InvestigationResultFreeText::getValue)
            .contains("RIT+++URINARY STONE weight 13 mg.",
                "RIT+++Consisted of 36% Calcium oxalate and 64% Calcium phosphate.");
    }

    @Test
    void testGetComplexReferenceRangeFreeTexts() {
        final var labResult = new LabResult(List.of(
            "ignore me",
            "FTX+CRR+++DVT Prophylaxis - INR 2.0-2.5",
            "ignore me",
            "FTX+CRR+++Treatment of DVT,PE,AF,TIA - INR 2.0-3.0",
            "ignore me"
        ));
        assertThat(labResult.getComplexReferenceRangeFreeTexts())
            .hasSize(2)
            .map(ComplexReferenceRangeFreeText::getValue)
            .contains("CRR+++DVT Prophylaxis - INR 2.0-2.5", "CRR+++Treatment of DVT,PE,AF,TIA - INR 2.0-3.0");
    }

    @Test
    void testGetSequenceReference() {
        final var labResult = new LabResult(List.of(
            "ignore me",
            "RFF+ASL:1",
            "ignore me"
        ));
        assertThat(labResult.getSequenceReference())
            .isNotNull()
            .extracting(SequenceReference::getValue)
            .isEqualTo("ASL:1");
    }

    @Test
    void testGetResultReferenceRanges() {
        final var labResult = new LabResult(List.of(
            "ignore me",
            "S20+20",
            "range #1 content",
            "S20+20",
            "range #2 content",
            "UNT+18+00000003",
            "ignore me"
        ));
        assertAll(
            () -> assertThat(labResult.getResultReferenceRanges()).hasSize(2)
                .first()
                .extracting(ResultReferenceRange::getEdifactSegments)
                .isEqualTo(List.of("S20+20", "range #1 content")),
            () -> assertThat(labResult.getResultReferenceRanges())
                .last()
                .extracting(ResultReferenceRange::getEdifactSegments)
                .isEqualTo(List.of("S20+20", "range #2 content"))
        );
    }

    @Test
    @SuppressWarnings("ResultOfMethodCallIgnored")
    void testLazyGettersWhenMissing() {
        final var labResult = new LabResult(List.of());
        assertAll(
            () -> assertThatThrownBy(labResult::getDiagnosticReportCode)
                .isExactlyInstanceOf(MissingSegmentException.class)
                .hasMessage("EDIFACT section is missing segment GIS"),
            () -> assertThatThrownBy(labResult::getLaboratoryInvestigation)
                .isExactlyInstanceOf(MissingSegmentException.class)
                .hasMessage("EDIFACT section is missing segment INV+MQ"),
            () -> assertThat(labResult.getSequenceDetails()).isEmpty(),
            () -> assertThat(labResult.getLaboratoryInvestigationResult()).isEmpty(),
            () -> assertThat(labResult.getTestStatus()).isEmpty(),
            () -> assertThat(labResult.getServiceProviderCommentFreeTexts()).isEmpty(),
            () -> assertThatThrownBy(labResult::getSequenceReference)
                .isExactlyInstanceOf(MissingSegmentException.class)
                .hasMessage("EDIFACT section is missing segment RFF"),
            () -> assertThat(labResult.getResultReferenceRanges()).isEmpty()
        );
    }
}
