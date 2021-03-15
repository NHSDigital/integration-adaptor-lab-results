package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import org.junit.jupiter.api.Test;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

class LaboratoryInvestigationResultTest {

    @Test
    void when_edifactStringDoesNotStartWithKey_expect_illegalArgumentExceptionIsThrown() {
        assertThatIllegalArgumentException().isThrownBy(
            () -> LaboratoryInvestigationResult.fromString("wrong value")
        ).withMessage("Can't create LaboratoryInvestigationResult from wrong value");
    }

    @Test
    void when_edifactString1IsPassed_expect_returnALaboratoryInvestigationNumericalResultObject() {
        final LaboratoryInvestigationResult laboratoryInvestigationNumericalResult =
            LaboratoryInvestigationResult.fromString("RSL+NV+11.9:7++:::ng/mL+HI");

        assertAll(
            () -> assertThat(laboratoryInvestigationNumericalResult.getMeasurementValue())
                .isEqualTo(new BigDecimal("11.9")),
            () -> assertThat(laboratoryInvestigationNumericalResult.getMeasurementValueComparator())
                .contains(MeasurementValueComparator.LESS_THAN),
            () -> assertThat(laboratoryInvestigationNumericalResult.getMeasurementUnit()).isEqualTo("ng/mL"),
            () -> assertThat(laboratoryInvestigationNumericalResult.getDeviatingResultIndicator())
                .isEqualTo(DeviatingResultIndicator.ABOVE_HIGH_REFERENCE_LIMIT)
        );
    }

    @Test
    void when_edifactString2IsPassed_expect_returnALaboratoryInvestigationNumericalResultObject() {
        final LaboratoryInvestigationResult laboratoryInvestigationNumericalResult =
            LaboratoryInvestigationResult.fromString("RSL+NV+11.9++:::ng/mL+HI");

        assertAll(
            () -> assertThat(laboratoryInvestigationNumericalResult.getMeasurementValue())
                .isEqualTo(new BigDecimal("11.9")),
            () -> assertThat(laboratoryInvestigationNumericalResult.getMeasurementValueComparator()).isEmpty(),
            () -> assertThat(laboratoryInvestigationNumericalResult.getMeasurementUnit()).isEqualTo("ng/mL"),
            () -> assertThat(laboratoryInvestigationNumericalResult.getDeviatingResultIndicator())
                .isEqualTo(DeviatingResultIndicator.ABOVE_HIGH_REFERENCE_LIMIT)
        );
    }

    @Test
    void when_edifactString3IsPassed_expect_returnALaboratoryInvestigationNumericalResultObject() {
        final LaboratoryInvestigationResult laboratoryInvestigationNumericalResult =
            LaboratoryInvestigationResult.fromString("RSL+NV+11.9++:::ng/mL");

        assertAll(
            () -> assertThat(laboratoryInvestigationNumericalResult.getMeasurementValue())
                .isEqualTo(new BigDecimal("11.9")),
            () -> assertThat(laboratoryInvestigationNumericalResult.getMeasurementValueComparator()).isEmpty(),
            () -> assertThat(laboratoryInvestigationNumericalResult.getMeasurementUnit()).isEqualTo("ng/mL"),
            () -> assertThat(laboratoryInvestigationNumericalResult.getDeviatingResultIndicator()).isNull()
        );
    }

    @Test
    void when_edifactString4IsPassed_expect_returnALaboratoryInvestigationCodedResultObject() {
        final LaboratoryInvestigationResult laboratoryInvestigationCodedResult =
            LaboratoryInvestigationResult.fromString("RSL+CV+::111222333:921::Cancer screening");

        assertAll(
            () -> assertThat(laboratoryInvestigationCodedResult.getCode()).isEqualTo("111222333"),
            () -> assertThat(laboratoryInvestigationCodedResult.getCodeType()).isEqualTo(CodingType.SNOMED_CT_CODE),
            () -> assertThat(laboratoryInvestigationCodedResult.getDescription()).isEqualTo("Cancer screening")
        );
    }

    @Test
    void when_buildingSegmentObjectWithoutAnyFields_expect_nullPointerExceptionIsThrown() {
        assertThatNullPointerException().isThrownBy(() -> LaboratoryInvestigation.builder().build());
    }

    @Test
    void testGetKey() {
        final LaboratoryInvestigationResult laboratoryInvestigationResult =
            LaboratoryInvestigationResult.builder()
                .measurementValue(new BigDecimal("11.9"))
                .measurementValueComparator(MeasurementValueComparator.LESS_THAN)
                .measurementUnit("ng/mL")
                .deviatingResultIndicator(DeviatingResultIndicator.ABOVE_HIGH_REFERENCE_LIMIT)
                .build();

        assertThat(laboratoryInvestigationResult.getKey()).isEqualTo("RSL");
    }

    @Test
    void testValidateNumericalResult() {
        final LaboratoryInvestigationResult laboratoryInvestigationNumericalResult =
            LaboratoryInvestigationResult.builder()
                .resultType(LaboratoryInvestigationResultType.NUMERICAL_VALUE)
                .measurementValue(new BigDecimal("11.9"))
                .measurementValueComparator(MeasurementValueComparator.LESS_THAN)
                .measurementUnit("ng/mL")
                .deviatingResultIndicator(DeviatingResultIndicator.ABOVE_HIGH_REFERENCE_LIMIT)
                .build();

        assertThatNoException().isThrownBy(laboratoryInvestigationNumericalResult::validate);

        LaboratoryInvestigationResult emptyMeasurementValue =
            LaboratoryInvestigationResult.builder()
                .resultType(LaboratoryInvestigationResultType.NUMERICAL_VALUE)
                .measurementValue(null)
                .measurementValueComparator(MeasurementValueComparator.LESS_THAN)
                .measurementUnit("ng/mL")
                .deviatingResultIndicator(DeviatingResultIndicator.ABOVE_HIGH_REFERENCE_LIMIT)
                .build();

        LaboratoryInvestigationResult emptyMeasurementUnit =
            LaboratoryInvestigationResult.builder()
                .resultType(LaboratoryInvestigationResultType.NUMERICAL_VALUE)
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

    @Test
    void testValidateCodedResult() {
        final LaboratoryInvestigationResult laboratoryInvestigationCodedResult =
            LaboratoryInvestigationResult.builder()
                .resultType(LaboratoryInvestigationResultType.CODED_VALUE)
                .code("111222333")
                .codeType(CodingType.SNOMED_CT_CODE)
                .description("Cancer screening")
                .build();

        assertThatNoException().isThrownBy(laboratoryInvestigationCodedResult::validate);

        LaboratoryInvestigationResult emptyCode =
            LaboratoryInvestigationResult.builder()
                .resultType(LaboratoryInvestigationResultType.CODED_VALUE)
                .code(null)
                .codeType(CodingType.SNOMED_CT_CODE)
                .description("Cancer screening")
                .build();

        LaboratoryInvestigationResult emptyCodeType =
            LaboratoryInvestigationResult.builder()
                .resultType(LaboratoryInvestigationResultType.CODED_VALUE)
                .code("111222333")
                .codeType(null)
                .description("Cancer screening")
                .build();

        LaboratoryInvestigationResult emptyDescription =
            LaboratoryInvestigationResult.builder()
                .resultType(LaboratoryInvestigationResultType.CODED_VALUE)
                .code("111222333")
                .codeType(CodingType.SNOMED_CT_CODE)
                .description(null)
                .build();

        assertAll(
            () -> assertThatThrownBy(emptyCode::validate)
                .isExactlyInstanceOf(EdifactValidationException.class)
                .hasMessage("RSL: Attribute code is required"),
            () -> assertThatThrownBy(emptyCodeType::validate)
                .isExactlyInstanceOf(EdifactValidationException.class)
                .hasMessage("RSL: Attribute codeType is required"),
            () -> assertThatThrownBy(emptyDescription::validate)
                .isExactlyInstanceOf(EdifactValidationException.class)
                .hasMessage("RSL: Attribute description is required")
        );
    }
}
