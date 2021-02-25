package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;

class PerformerNameAndAddressTest {

    private final PerformerNameAndAddress performingOrganisationNameAndAddress = PerformerNameAndAddress.builder()
        .performingOrganisationName("LONDON CITY HOSPITAL")
        .build();

    private final PerformerNameAndAddress performerNameAndAddress = PerformerNameAndAddress.builder()
            .identifier("A2442389")
            .code(HealthcareRegistrationIdentificationCode.CONSULTANT)
            .performerName("DR J SMITH")
            .build();

    @Test
    void when_edifactStringDoesNotStartWithCorrectKey_expect_illegalArgumentExceptionIsThrown() {
        assertThatThrownBy(() -> PerformerNameAndAddress.fromString("wrong value"))
            .isExactlyInstanceOf(IllegalArgumentException.class)
            .hasMessage("Can't create PerformerNameAndAddress from wrong value");
    }

    @Test
    void when_edifactStringIsPassed_expect_returnAPerformingOrganisationNameAndAddressObject() {
        assertThat(performingOrganisationNameAndAddress)
            .usingRecursiveComparison()
            .isEqualTo(PerformerNameAndAddress.fromString("NAD+SLA+++LONDON CITY HOSPITAL"));
    }

    @Test
    void when_edifactStringIsPassed_expect_returnAPerformerNameAndAddressObject() {
        assertThat(performerNameAndAddress)
            .usingRecursiveComparison()
            .isEqualTo(PerformerNameAndAddress.fromString("NAD+SLA+A2442389:902++DR J SMITH"));
    }

    @Test
    void when_buildingSegmentObjectWithEmptyHealthcareCode_expect_illegalArgumentExceptionIsThrown() {
        final var builder = PerformerNameAndAddress.builder()
            .identifier("A2442389")
            .performingOrganisationName("")
            .performerName("");

        assertThatThrownBy(() -> builder.code(HealthcareRegistrationIdentificationCode.fromCode("")))
            .isExactlyInstanceOf(IllegalArgumentException.class)
            .hasMessage("No HealthcareRegistrationIdentificationCode for ''");

    }

    @Test
    void testGetKey() {
        assertThat(performingOrganisationNameAndAddress.getKey()).isEqualTo("NAD");
    }

    @Test
    void testGetPerformingOrganisationName() {
        assertThat(performingOrganisationNameAndAddress.getPerformingOrganisationName())
            .isEqualTo("LONDON CITY HOSPITAL");
    }

    @Test
    void testGetPerformerName() {
        assertThat(performerNameAndAddress.getPerformerName()).isEqualTo("DR J SMITH");
    }

    @Test
    void testValidateMissingPerformingOrganisationName() {
        var emptyPerformingOrganisationName = new PerformerNameAndAddress("", null, "", "");

        assertThatThrownBy(emptyPerformingOrganisationName::validate)
            .isExactlyInstanceOf(EdifactValidationException.class)
            .hasMessage("NAD: Attribute performingOrganisationName is required");
    }

    @Test
    void testValidateMissingCode() {
        var emptyCode = new PerformerNameAndAddress("Identifier", null, "", "DR J SMITH");

        assertThatThrownBy(emptyCode::validate)
            .isExactlyInstanceOf(EdifactValidationException.class)
            .hasMessage("NAD: Attribute code is required");
    }

    @Test
    void testValidateMissingPerformerName() {
        var emptyPerformerName = new PerformerNameAndAddress(
            "Identifier",
            HealthcareRegistrationIdentificationCode.CONSULTANT,
            null,
            null
        );

        assertThatThrownBy(emptyPerformerName::validate)
            .isExactlyInstanceOf(EdifactValidationException.class)
            .hasMessage("NAD: Attribute performerName is required");
    }
}
