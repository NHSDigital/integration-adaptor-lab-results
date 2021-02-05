package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import org.junit.jupiter.api.Test;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LaboratoryInvestigationTest {

    private final LaboratoryInvestigation laboratoryInvestigation = new LaboratoryInvestigation(
        "42R4.", "Serum ferritin"
    );

    @Test
    void when_edifactStringDoesNotStartWithLaboratoryInvestigationKey_expect_illegalArgumentExceptionIsThrown() {
        assertThrows(IllegalArgumentException.class, () -> LaboratoryInvestigation.fromString("wrong value"));
    }

    @Test
    void when_edifactStringIsPassed_expect_returnALaboratoryInvestigationObject() {
        assertThat(laboratoryInvestigation)
            .usingRecursiveComparison()
            .isEqualTo(LaboratoryInvestigation.fromString("INV+MQ+42R4.:911::Serum ferritin"));
    }

    @Test
    void when_mappingSegmentObjectToEdifactString_expect_returnCorrectEdifactString() {
        String expectedEdifactString = "INV+MQ+42R4.:911::Serum ferritin'";

        LaboratoryInvestigation laboratoryInvestigation = LaboratoryInvestigation.builder()
            .investigationCode("42R4.")
            .investigationDescription("Serum ferritin")
            .build();

        assertEquals(expectedEdifactString, laboratoryInvestigation.toEdifact());
    }

    @Test
    void when_mappingSegmentObjectToEdifactStringWithEmptyDescriptionField_expect_edifactValidationExceptionIsThrown() {
        LaboratoryInvestigation laboratoryInvestigation = LaboratoryInvestigation.builder()
            .investigationCode("42R4.")
            .investigationDescription("")
            .build();

        assertThrows(EdifactValidationException.class, laboratoryInvestigation::toEdifact);
    }

    @Test
    void when_buildingSegmentObjectWithoutAnyFields_expect_nullPointerExceptionIsThrown() {
        assertThrows(NullPointerException.class, () -> LaboratoryInvestigation.builder().build());
    }

    @Test
    void testGetKey() {
        assertEquals(laboratoryInvestigation.getKey(), "INV");
    }

    @Test
    void testGetValue() {
        assertEquals(laboratoryInvestigation.getValue(), "MQ+42R4.:911::Serum ferritin");
    }

    @Test
    void testValidateStateful() {
        assertDoesNotThrow(laboratoryInvestigation::validateStateful);
    }

    @Test
    void testPreValidate() {
        LaboratoryInvestigation emptyDescription = new LaboratoryInvestigation(
                "42R4.", ""
        );

        assertThatThrownBy(emptyDescription::preValidate)
                .isExactlyInstanceOf(EdifactValidationException.class)
                .hasMessage("INV: Attribute investigationDescription is required");
    }
}
