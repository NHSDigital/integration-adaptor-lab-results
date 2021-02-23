package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import org.junit.jupiter.api.Test;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PerformerNameAndAddressTest {

    private final PerformerNameAndAddress performingOrganisationNameAndAddress =
        new PerformerNameAndAddress("", null, "LONDON CITY HOSPITAL", "");

    private final PerformerNameAndAddress performerNameAndAddress =
        new PerformerNameAndAddress(
            "A2442389",
            HealthcareRegistrationIdentificationCode.CONSULTANT,
            "",
            "DR J SMITH"
        );

    @Test
    void when_edifactStringDoesNotStartWithCorrectKey_expect_illegalArgumentExceptionIsThrown() {
        assertThrows(IllegalArgumentException.class,
            () -> PerformerNameAndAddress.fromString("wrong value"));
    }

    @Test
    void when_edifactStringIsPassed_expect_returnAPerformingOrganisationNameAndAddressObject() {
        assertAll(
            () -> assertThat(performingOrganisationNameAndAddress)
                .usingRecursiveComparison()
                .isEqualTo(PerformerNameAndAddress.fromString("NAD+SLA+++LONDON CITY HOSPITAL")),

            () -> assertThat(performerNameAndAddress)
                .usingRecursiveComparison()
                .isEqualTo(PerformerNameAndAddress.fromString("NAD+SLA+A2442389:902++DR J SMITH"))
        );
    }

    @Test
    void when_buildingSegmentObjectWithEmptyHealthcareCode_expect_illegalArgumentExceptionIsThrown() {
        assertThrows(
            IllegalArgumentException.class,
            () -> PerformerNameAndAddress.builder()
                .identifier("A2442389")
                .code(HealthcareRegistrationIdentificationCode.fromCode(""))
                .performingOrganisationName("")
                .performerName("")
                .build()
        );
    }

    @Test
    void when_buildingSegmentObjectWithoutMandatoryField_expect_nullPointerExceptionIsThrown() {
        assertThrows(NullPointerException.class, () -> PerformerNameAndAddress.builder().build());
    }

    @Test
    void testGetKey() {
        assertEquals("NAD", performingOrganisationNameAndAddress.getKey());
    }

    @Test
    void testGetPerformingOrganisationName() {
        assertEquals("LONDON CITY HOSPITAL", performingOrganisationNameAndAddress.getPerformingOrganisationName());
    }

    @Test
    void testGetPerformerName() {
        assertEquals("DR J SMITH", performerNameAndAddress.getPerformerName());
    }

    @Test
    void testValidate() {
        var emptyPerformingOrganisationName = new PerformerNameAndAddress("", null, "", "");

        assertThatThrownBy(emptyPerformingOrganisationName::validate)
            .isExactlyInstanceOf(EdifactValidationException.class)
            .hasMessage("NAD: Attribute performingOrganisationName is required");
    }
}
