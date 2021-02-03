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

class LaboratoryInvestigationResultTest {

    private final LaboratoryInvestigationResult laboratoryInvestigationResult = new LaboratoryInvestigationResult(
            new BigDecimal("11.9"), MeasurementValueComparator.LESS_THAN, "ng/mL", DeviatingResultIndicator.ABOVE_HIGH_REFERENCE_LIMIT
    );

    @Test
    void when_edifactStringDoesNotStartWithLaboratoryInvestigationResultKey_expect_illegalArgumentExceptionIsThrown() {
        assertThrows(IllegalArgumentException.class, () -> LaboratoryInvestigationResult.fromString("wrong value"));
    }

    @Test
    void when_edifactString1IsPassed_expect_returnALaboratoryInvestigationResultObject() {
        assertThat(laboratoryInvestigationResult)
            .usingRecursiveComparison()
            .isEqualTo(LaboratoryInvestigationResult.fromString("RSL+NV+11.9:7++:::ng/mL+HI"));
    }

    @Test
    void when_edifactString2IsPassed_expect_returnALaboratoryInvestigationResultObject() {
        final LaboratoryInvestigationResult laboratoryInvestigationResult = new LaboratoryInvestigationResult(
                new BigDecimal("11.9"), null, "ng/mL", DeviatingResultIndicator.ABOVE_HIGH_REFERENCE_LIMIT
        );

        assertThat(laboratoryInvestigationResult)
                .usingRecursiveComparison()
                .isEqualTo(LaboratoryInvestigationResult.fromString("RSL+NV+11.9++:::ng/mL+HI"));
    }

    @Test
    void when_edifactString3IsPassed_expect_returnALaboratoryInvestigationResultObject() {
        final LaboratoryInvestigationResult laboratoryInvestigationResult = new LaboratoryInvestigationResult(
                new BigDecimal("11.9"), null, "ng/mL", null
        );

        assertThat(laboratoryInvestigationResult)
                .usingRecursiveComparison()
                .isEqualTo(LaboratoryInvestigationResult.fromString("RSL+NV+11.9++:::ng/mL"));
    }

    @Test
    void when_mappingSegmentObjectToEdifactString_expect_returnCorrectEdifactString() {
        String expectedEdifactString = "RSL+NV+11.9:7++:::ng/mL+HI'";

        LaboratoryInvestigationResult laboratoryInvestigationResult = LaboratoryInvestigationResult.builder()
            .measurementValue(new BigDecimal("11.9"))
            .measurementValueComparator(MeasurementValueComparator.LESS_THAN)
            .measurementUnit("ng/mL")
            .deviatingResultIndicator(DeviatingResultIndicator.ABOVE_HIGH_REFERENCE_LIMIT)
            .build();

        assertEquals(expectedEdifactString, laboratoryInvestigationResult.toEdifact());
    }

    @Test
    void when_buildingSegmentObjectWithoutAnyFields_expect_nullPointerExceptionIsThrown() {
        assertThrows(NullPointerException.class, () -> LaboratoryInvestigation.builder().build());
    }

    @Test
    void testGetKey() {
        assertEquals(laboratoryInvestigationResult.getKey(), "RSL");
    }

    @Test
    void testGetValue() {
        assertEquals(laboratoryInvestigationResult.getValue(), "NV+11.9:7++:::ng/mL+HI");
    }

    @Test
    void testValidateStateful() {
        assertDoesNotThrow(laboratoryInvestigationResult::validateStateful);
    }

    @Test
    void testPreValidate() {
        LaboratoryInvestigationResult emptyMeasurementValue = new LaboratoryInvestigationResult(
                null, MeasurementValueComparator.LESS_THAN, "ng/mL", DeviatingResultIndicator.ABOVE_HIGH_REFERENCE_LIMIT
        );
        LaboratoryInvestigationResult emptyMeasurementUnit = new LaboratoryInvestigationResult(
                new BigDecimal("11.9"), MeasurementValueComparator.LESS_THAN, null, DeviatingResultIndicator.ABOVE_HIGH_REFERENCE_LIMIT
        );

        assertAll(
            () -> assertThatThrownBy(emptyMeasurementValue::preValidate)
                    .isExactlyInstanceOf(EdifactValidationException.class)
                    .hasMessage("RSL: Attribute measurementValue is required"),
            () -> assertThatThrownBy(emptyMeasurementUnit::preValidate)
                    .isExactlyInstanceOf(EdifactValidationException.class)
                    .hasMessage("RSL: Attribute measurementUnit is required")
        );
    }
}
