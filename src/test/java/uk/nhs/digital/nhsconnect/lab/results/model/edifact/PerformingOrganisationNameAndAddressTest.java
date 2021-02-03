package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import org.junit.jupiter.api.Test;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PerformingOrganisationNameAndAddressTest {

    private final PerformingOrganisationNameAndAddress performingOrganisationNameAndAddress =
        new PerformingOrganisationNameAndAddress("LONDON CITY HOSPITAL");

    @Test
    void when_edifactStringDoesNotStartWithCorrectKey_expect_illegalArgumentExceptionIsThrown() {
        assertThrows(IllegalArgumentException.class, () -> PerformingOrganisationNameAndAddress.fromString("wrong value"));
    }

    @Test
    void when_edifactStringIsPassed_expect_returnAPerformingOrganisationNameAndAddressObject() {
        assertThat(performingOrganisationNameAndAddress)
            .usingRecursiveComparison()
            .isEqualTo(PerformingOrganisationNameAndAddress.fromString("NAD+SLA+++LONDON CITY HOSPITAL"));
    }

    @Test
    void when_mappingSegmentObjectToEdifactString_expect_returnCorrectEdifactString() {
        String expectedEdifactString = "NAD+SLA+++LONDON CITY HOSPITAL'";

        PerformingOrganisationNameAndAddress performingOrganisation = PerformingOrganisationNameAndAddress.builder()
                .performingOrganisationName("LONDON CITY HOSPITAL")
                .build();

        assertEquals(expectedEdifactString, performingOrganisation.toEdifact());
    }

    @Test
    void when_mappingSegmentObjectToEdifactStringWithEmptyField_expect_edifactValidationExceptionIsThrown() {
        PerformingOrganisationNameAndAddress performingOrganisation = PerformingOrganisationNameAndAddress.builder()
                .performingOrganisationName("")
                .build();

        assertThrows(EdifactValidationException.class, performingOrganisation::toEdifact);
    }

    @Test
    void when_buildingSegmentObjectWithoutMandatoryField_expect_nullPointerExceptionIsThrown() {
        assertThrows(NullPointerException.class, () -> PerformingOrganisationNameAndAddress.builder().build());
    }

    @Test
    void testGetKey() {
        assertEquals(performingOrganisationNameAndAddress.getKey(), "NAD");
    }

    @Test
    void testGetValue() {
        assertEquals(performingOrganisationNameAndAddress.getValue(), "SLA+++LONDON CITY HOSPITAL");
    }

    @Test
    void testValidateStateful() {
        assertDoesNotThrow(performingOrganisationNameAndAddress::validateStateful);
    }

    @Test
    void testPreValidate() {
        PerformingOrganisationNameAndAddress emptyPerformingOrganisationName = new PerformingOrganisationNameAndAddress("");

        assertThatThrownBy(emptyPerformingOrganisationName::preValidate)
            .isExactlyInstanceOf(EdifactValidationException.class)
            .hasMessage("NAD: Attribute performingOrganisationName is required");
    }
}
