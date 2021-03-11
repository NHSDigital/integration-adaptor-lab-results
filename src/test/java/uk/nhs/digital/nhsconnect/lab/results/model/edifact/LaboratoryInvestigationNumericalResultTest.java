package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import org.junit.jupiter.api.Test;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LaboratoryInvestigationNumericalResultTest {

    private final LaboratoryInvestigationNumericalResult laboratoryInvestigationNumericalResult =
        LaboratoryInvestigationNumericalResult.builder()
            .measurementValue(new BigDecimal("11.9"))
            .measurementValueComparator(MeasurementValueComparator.LESS_THAN)
            .measurementUnit("ng/mL")
            .deviatingResultIndicator(DeviatingResultIndicator.ABOVE_HIGH_REFERENCE_LIMIT)
            .build();

    @Test
    void when_edifactStringDoesNotStartWithKey_expect_illegalArgumentExceptionIsThrown() {
        assertThrows(
            IllegalArgumentException.class, () -> LaboratoryInvestigationNumericalResult.fromString("wrong value")
        );
    }

    @Test
    void when_edifactString1IsPassed_expect_returnALaboratoryInvestigationNumericalResultObject() {
        assertThat(laboratoryInvestigationNumericalResult)
            .usingRecursiveComparison()
            .isEqualTo(LaboratoryInvestigationNumericalResult.fromString("RSL+NV+11.9:7++:::ng/mL+HI"));
    }

    @Test
    void when_edifactString2IsPassed_expect_returnALaboratoryInvestigationNumericalResultObject() {
        final LaboratoryInvestigationNumericalResult laboratoryInvestigationNumericalResult =
            LaboratoryInvestigationNumericalResult.builder()
                .measurementValue(new BigDecimal("11.9"))
                .measurementValueComparator(null)
                .measurementUnit("ng/mL")
                .deviatingResultIndicator(DeviatingResultIndicator.ABOVE_HIGH_REFERENCE_LIMIT)
                .build();

        assertThat(laboratoryInvestigationNumericalResult)
            .usingRecursiveComparison()
            .isEqualTo(LaboratoryInvestigationNumericalResult.fromString("RSL+NV+11.9++:::ng/mL+HI"));
    }

    @Test
    void when_edifactString3IsPassed_expect_returnALaboratoryInvestigationNumericalResultObject() {
        final LaboratoryInvestigationNumericalResult laboratoryInvestigationNumericalResult =
            LaboratoryInvestigationNumericalResult.builder()
                .measurementValue(new BigDecimal("11.9"))
                .measurementValueComparator(null)
                .measurementUnit("ng/mL")
                .deviatingResultIndicator(null)
                .build();

        assertThat(laboratoryInvestigationNumericalResult)
            .usingRecursiveComparison()
            .isEqualTo(LaboratoryInvestigationNumericalResult.fromString("RSL+NV+11.9++:::ng/mL"));
    }

    @Test
    void when_buildingSegmentObjectWithoutAnyFields_expect_nullPointerExceptionIsThrown() {
        assertThrows(NullPointerException.class, () -> LaboratoryInvestigation.builder().build());
    }

    @Test
    void testGetKey() {
        assertEquals(laboratoryInvestigationNumericalResult.getKey(), "RSL");
    }

    @Test
    void testValidate() {
        assertDoesNotThrow(laboratoryInvestigationNumericalResult::validate);

        LaboratoryInvestigationNumericalResult emptyMeasurementValue =
            LaboratoryInvestigationNumericalResult.builder()
                .measurementValue(null)
                .measurementValueComparator(MeasurementValueComparator.LESS_THAN)
                .measurementUnit("ng/mL")
                .deviatingResultIndicator(DeviatingResultIndicator.ABOVE_HIGH_REFERENCE_LIMIT)
                .build();

        LaboratoryInvestigationNumericalResult emptyMeasurementUnit =
            LaboratoryInvestigationNumericalResult.builder()
                .measurementValue(new BigDecimal("11.9"))
                .measurementValueComparator(MeasurementValueComparator.LESS_THAN)
                .measurementUnit(null)
                .deviatingResultIndicator(DeviatingResultIndicator.ABOVE_HIGH_REFERENCE_LIMIT)
                .build();

        assertAll(
            () -> assertThatThrownBy(emptyMeasurementValue::validate)
                .isExactlyInstanceOf(EdifactValidationException.class)
                .hasMessage("RSL: Attribute measurementValue is required"),
            () -> assertThatThrownBy(emptyMeasurementUnit::validate)
                .isExactlyInstanceOf(EdifactValidationException.class)
                .hasMessage("RSL: Attribute measurementUnit is required")
        );
    }
}
