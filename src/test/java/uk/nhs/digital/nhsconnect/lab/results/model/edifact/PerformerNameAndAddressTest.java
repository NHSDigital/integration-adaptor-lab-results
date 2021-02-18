package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import org.junit.jupiter.api.Test;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.NoSuchElementException;

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
    void when_mappingSegmentObjectToEdifactString_expect_returnCorrectEdifactString() {
        String expectedEdifactString = "NAD+SLA+++LONDON CITY HOSPITAL'";
        String expectedPerformerEdifactString = "NAD+SLA+A2442389:902++DR J SMITH'";

        PerformerNameAndAddress performingOrganisation = PerformerNameAndAddress.builder()
            .identifier("")
            .code(null)
            .performingOrganisationName("LONDON CITY HOSPITAL")
            .partyName("")
            .build();

        PerformerNameAndAddress performer = PerformerNameAndAddress.builder()
            .identifier("A2442389")
            .code(HealthcareRegistrationIdentificationCode.CONSULTANT)
            .performingOrganisationName("")
            .partyName("DR J SMITH")
            .build();

        assertAll(
            () -> assertEquals(expectedEdifactString, performingOrganisation.toEdifact()),
            () -> assertEquals(expectedPerformerEdifactString, performer.toEdifact())
        );
    }

    @Test
    void when_mappingSegmentObjectToEdifactStringWithEmptyField_expect_edifactValidationExceptionIsThrown() {
        PerformerNameAndAddress performingOrganisation = PerformerNameAndAddress.builder()
            .identifier("")
            .code(null)
            .performingOrganisationName("")
            .partyName("")
            .build();

        PerformerNameAndAddress performerWithoutName = PerformerNameAndAddress.builder()
            .identifier("A2442389")
            .code(HealthcareRegistrationIdentificationCode.CONSULTANT)
            .performingOrganisationName("")
            .partyName("")
            .build();

        assertAll(
            () -> assertThrows(EdifactValidationException.class, performingOrganisation::toEdifact),
            () -> assertThrows(EdifactValidationException.class, performerWithoutName::toEdifact)
        );
    }

    @Test
    void when_buildingSegmentObjectWithEmptyHealthcareCode_expect_noSuchElementExceptionIsThrown() {
        assertThrows(
            NoSuchElementException.class,
            () -> PerformerNameAndAddress.builder()
                .identifier("A2442389")
                .code(HealthcareRegistrationIdentificationCode.fromCode(""))
                .performingOrganisationName("")
                .partyName("")
                .build()
        );
    }

    @Test
    void when_buildingSegmentObjectWithoutMandatoryField_expect_nullPointerExceptionIsThrown() {
        assertThrows(NullPointerException.class, () -> PerformerNameAndAddress.builder().build());
    }

    @Test
    void testGetKey() {
        assertEquals(performingOrganisationNameAndAddress.getKey(), "NAD");
    }

    @Test
    void testGetValue() {
        assertAll(
            () -> assertEquals(performingOrganisationNameAndAddress.getValue(), "SLA+++LONDON CITY HOSPITAL"),
            () -> assertEquals(performerNameAndAddress.getValue(), "SLA+A2442389:902++DR J SMITH")
        );

    }

    @Test
    void testValidateStateful() {
        assertDoesNotThrow(performingOrganisationNameAndAddress::validateStateful);
    }

    @Test
    void testPreValidate() {
        var emptyPerformingOrganisationName = new PerformerNameAndAddress("", null, "", "");

        assertThatThrownBy(emptyPerformingOrganisationName::preValidate)
            .isExactlyInstanceOf(EdifactValidationException.class)
            .hasMessage("NAD: Attribute performingOrganisationName is required");
    }
}
