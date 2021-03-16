package uk.nhs.digital.nhsconnect.lab.results.model.edifact.segmentgroup;

import org.junit.jupiter.api.Test;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.CodingType;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.DeviatingResultIndicator;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.DiagnosticReportCode;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.FreeTextSegment;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.FreeTextType;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.LaboratoryInvestigation;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.LaboratoryInvestigationResult;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.MeasurementValueComparator;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Reference;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.ReferenceType;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.SequenceDetails;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.TestStatus;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.TestStatusCode;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.MissingSegmentException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

class LabResultTest {

    public static final double EXPECTED_MEASUREMENT_VALUE = 11.9;

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
        assertThat(labResult.getReportCode())
            .isNotNull()
            .extracting(DiagnosticReportCode::getCode)
            .isEqualTo("N");
    }

    @Test
    void testGetLaboratoryInvestigation() {
        final var labResult = new LabResult(List.of(
            "ignore me",
            "INV+MQ+42R4.:911::Serum ferritin",
            "ignore me"
        ));
        var labResultInvestigation = assertThat(labResult.getInvestigation()).isNotNull();

        labResultInvestigation
            .extracting(LaboratoryInvestigation::getCode)
            .isEqualTo(Optional.of("42R4."));
        labResultInvestigation
            .extracting(LaboratoryInvestigation::getDescription)
            .isEqualTo("Serum ferritin");
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
            .map(SequenceDetails::getNumber)
            .contains("ABC123");
    }

    @Test
    void testGetLaboratoryInvestigationNumericalResult() {
        final var labResult = new LabResult(List.of(
            "ignore me",
            "RSL+NV+11.9:7++:::ng/mL+HI",
            "ignore me"
        ));
        var labResultInvestigationResult = assertThat(labResult.getInvestigationResult()).isPresent();

        labResultInvestigationResult
            .map(LaboratoryInvestigationResult::getDeviatingResultIndicator)
            .hasValue(DeviatingResultIndicator.ABOVE_HIGH_REFERENCE_LIMIT);
        labResultInvestigationResult
            .map(LaboratoryInvestigationResult::getMeasurementUnit)
            .hasValue("ng/mL");
        labResultInvestigationResult
            .map(LaboratoryInvestigationResult::getMeasurementValue)
            .hasValue(BigDecimal.valueOf(EXPECTED_MEASUREMENT_VALUE));
        labResultInvestigationResult
            .map(LaboratoryInvestigationResult::getMeasurementValueComparator)
            .contains(Optional.of(MeasurementValueComparator.LESS_THAN));
    }

    @Test
    void testGetLaboratoryInvestigationCodedResult() {
        final var labResult = new LabResult(List.of(
            "ignore me",
            "RSL+CV+::375211000000108:921::Bowel cancer screening programme FOB test normal (finding)",
            "ignore me"
        ));
        var labResultInvestigationResult = assertThat(labResult.getInvestigationResult()).isPresent();

        labResultInvestigationResult
            .map(LaboratoryInvestigationResult::getCode)
            .hasValue("375211000000108");
        labResultInvestigationResult
            .map(LaboratoryInvestigationResult::getCodingType)
            .contains(Optional.of(CodingType.SNOMED_CT_CODE));
        labResultInvestigationResult
            .map(LaboratoryInvestigationResult::getDescription)
            .hasValue("Bowel cancer screening programme FOB test normal (finding)");
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
            .map(TestStatus::getTestStatusCode)
            .contains(TestStatusCode.CORRECTED);
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
        var labResultFreeTexts = assertThat(labResult.getFreeTexts()).hasSize(2);

        labResultFreeTexts
            .map(FreeTextSegment::getType)
            .isEqualTo(List.of(FreeTextType.SERVICE_PROVIDER_COMMENT, FreeTextType.SERVICE_PROVIDER_COMMENT));
        labResultFreeTexts
            .map(FreeTextSegment::getTexts)
            .map(values -> values[0])
            .isEqualTo(List.of("red blood cell seen", "Note low platelets"));
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

        var labResultFreeTexts = assertThat(labResult.getFreeTexts()).hasSize(2);

        labResultFreeTexts
            .map(FreeTextSegment::getType)
            .isEqualTo(List.of(FreeTextType.INVESTIGATION_RESULT, FreeTextType.INVESTIGATION_RESULT));
        labResultFreeTexts
            .map(FreeTextSegment::getTexts)
            .map(values -> values[0])
            .isEqualTo(List.of(
                "URINARY STONE weight 13 mg.", "Consisted of 36% Calcium oxalate and 64% Calcium phosphate."));
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

        var labResultFreeTexts = assertThat(labResult.getFreeTexts()).hasSize(2);

        labResultFreeTexts
            .map(FreeTextSegment::getType)
            .isEqualTo(List.of(FreeTextType.COMPLEX_REFERENCE_RANGE, FreeTextType.COMPLEX_REFERENCE_RANGE));
        labResultFreeTexts
            .map(FreeTextSegment::getTexts)
            .map(values -> values[0])
            .isEqualTo(List.of("DVT Prophylaxis - INR 2.0-2.5", "Treatment of DVT,PE,AF,TIA - INR 2.0-3.0"));
    }

    @Test
    void testGetSequenceReference() {
        final var labResult = new LabResult(List.of(
            "ignore me",
            "RFF+ASL:1",
            "ignore me"
        ));
        var labResultSequenceReference = assertThat(labResult.getSequenceReference()).isNotNull();

        labResultSequenceReference
            .extracting(Reference::getNumber)
            .isEqualTo("1");
        labResultSequenceReference
            .extracting(Reference::getTarget)
            .extracting(ReferenceType::getQualifier)
            .isEqualTo("ASL");
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
            () -> assertThatThrownBy(labResult::getReportCode)
                .isExactlyInstanceOf(MissingSegmentException.class)
                .hasMessage("EDIFACT section is missing segment GIS"),
            () -> assertThatThrownBy(labResult::getInvestigation)
                .isExactlyInstanceOf(MissingSegmentException.class)
                .hasMessage("EDIFACT section is missing segment INV+MQ"),
            () -> assertThat(labResult.getSequenceDetails()).isEmpty(),
            () -> assertThat(labResult.getInvestigationResult()).isEmpty(),
            () -> assertThat(labResult.getTestStatus()).isEmpty(),
            () -> assertThat(labResult.getFreeTexts()).isEmpty(),
            () -> assertThatThrownBy(labResult::getSequenceReference)
                .isExactlyInstanceOf(MissingSegmentException.class)
                .hasMessage("EDIFACT section is missing segment RFF"),
            () -> assertThat(labResult.getResultReferenceRanges()).isEmpty()
        );
    }
}
