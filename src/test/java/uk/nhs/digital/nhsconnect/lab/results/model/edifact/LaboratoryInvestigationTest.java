package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import org.junit.jupiter.api.Test;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LaboratoryInvestigationTest {

    @Test
    void when_edifactStringDoesNotStartWithLaboratoryInvestigationKey_expect_illegalArgumentExceptionIsThrown() {
        assertThatThrownBy(() -> LaboratoryInvestigation.fromString("wrong value"))
            .isExactlyInstanceOf(IllegalArgumentException.class)
            .hasMessage("Can't create LaboratoryInvestigation from wrong value");
    }

    @Test
    void when_edifactStringIsPassed_expect_returnALaboratoryInvestigationObject() {
        final var laboratoryInvestigation = LaboratoryInvestigation.builder()
            .investigationCode("42R4.")
            .investigationCodeType(CodingType.READ_CODE)
            .investigationDescription("Serum ferritin")
            .build();

        assertThat(laboratoryInvestigation)
            .usingRecursiveComparison()
            .isEqualTo(LaboratoryInvestigation.fromString("INV+MQ+42R4.:911::Serum ferritin"));
    }

    @Test
    void when_buildingSegmentObjectWithoutAnyFields_expect_nullPointerExceptionIsThrown() {
        assertThatThrownBy(() -> LaboratoryInvestigation.builder().build())
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("investigationDescription is marked non-null but is null");
    }

    @Test
    void testGetKey() {
        assertThat(new LaboratoryInvestigation(null, CodingType.READ_CODE, ".").getKey()).isEqualTo("INV");
    }

    @Test
    void testValidateAllValues() {
        final var laboratoryInvestigation = LaboratoryInvestigation.builder()
            .investigationCode("43J7.")
            .investigationCodeType(CodingType.READ_CODE)
            .investigationDescription("IgE")
            .build();

        assertThatNoException().isThrownBy(laboratoryInvestigation::validate);
    }

    @Test
    void testValidateMissingCode() {
        LaboratoryInvestigation laboratoryInvestigation = LaboratoryInvestigation.builder()
            .investigationCode(null)
            .investigationCodeType(CodingType.READ_CODE)
            .investigationDescription("This is an investigation description")
            .build();

        assertThatNoException().isThrownBy(laboratoryInvestigation::validate);
    }

    @Test
    void testValidateMissingDescription() {
        final var laboratoryInvestigation = LaboratoryInvestigation.builder()
            .investigationCode("42R4.")
            .investigationCodeType(CodingType.READ_CODE)
            .investigationDescription("")
            .build();

        assertThatThrownBy(laboratoryInvestigation::validate)
            .isExactlyInstanceOf(EdifactValidationException.class)
            .hasMessage("INV: Attribute investigationDescription is required");
    }
}
