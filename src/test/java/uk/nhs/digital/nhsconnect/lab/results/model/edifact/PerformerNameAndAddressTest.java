package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;

class PerformerNameAndAddressTest {

    private final PerformerNameAndAddress performingPractitioner = PerformerNameAndAddress.builder()
        .identifier("A2442389")
        .code(HealthcareRegistrationIdentificationCode.CONSULTANT)
        .practitionerName("DR J SMITH")
        .build();

    private final PerformerNameAndAddress performingOrganization = PerformerNameAndAddress.builder()
        .partyName("ST JAMES?'S UNIVERSITY HOSPITAL")
        .build();

    @Test
    void when_edifactStringDoesNotStartWithCorrectKey_expect_illegalArgumentExceptionIsThrown() {
        assertThatThrownBy(() -> PerformerNameAndAddress.fromString("wrong value"))
            .isExactlyInstanceOf(IllegalArgumentException.class)
            .hasMessage("Can't create PerformerNameAndAddress from wrong value");
    }

    @Test
    void when_edifactStringIsPassed_expect_returnAPerformingOrganizationNameAndAddressObject() {
        assertThat(PerformerNameAndAddress.fromString("NAD+SLA+++ST JAMES?'S UNIVERSITY HOSPITAL"))
            .usingRecursiveComparison()
            .isEqualTo(performingOrganization);
    }

    @Test
    void when_edifactStringIsPassed_expect_returnAPerformingPractitionerNameAndAddressObject() {
        assertThat(PerformerNameAndAddress.fromString("NAD+SLA+A2442389:902++DR J SMITH"))
            .usingRecursiveComparison()
            .isEqualTo(performingPractitioner);
    }

    @Test
    void when_buildingSegmentObjectWithEmptyHealthcareCode_expect_illegalArgumentExceptionIsThrown() {
        final var builder = PerformerNameAndAddress.builder()
            .identifier("A2442389")
            .partyName(null)
            .practitionerName(null);

        assertThatThrownBy(() -> builder.code(HealthcareRegistrationIdentificationCode.fromCode("")))
            .isExactlyInstanceOf(IllegalArgumentException.class)
            .hasMessage("No HealthcareRegistrationIdentificationCode for ''");

    }

    @Test
    void testGetKey() {
        assertThat(performingOrganization.getKey()).isEqualTo("NAD");
    }

    @Test
    void testGetPerformingOrganizationName() {
        assertThat(performingOrganization.getPartyName())
            .isEqualTo("ST JAMES?'S UNIVERSITY HOSPITAL");
    }

    @Test
    void testGetPractitionerName() {
        assertThat(performingPractitioner.getPractitionerName()).isEqualTo("DR J SMITH");
    }

    @Test
    void testValidateMissingIdentifier() {
        var emptyIdentifier = PerformerNameAndAddress.builder()
            .identifier(null)
            .code(HealthcareRegistrationIdentificationCode.GP)
            .practitionerName("SMITH")
            .partyName(null)
            .build();

        assertThatThrownBy(emptyIdentifier::validate)
            .isExactlyInstanceOf(EdifactValidationException.class)
            .hasMessage("NAD: Attribute identifier is required");
    }

    @Test
    void testValidateMissingCode() {
        var emptyCode = PerformerNameAndAddress.builder()
            .identifier("identifier")
            .code(null)
            .practitionerName("DR J SMITH")
            .partyName(null)
            .build();

        assertThatThrownBy(emptyCode::validate)
            .isExactlyInstanceOf(EdifactValidationException.class)
            .hasMessage("NAD: Attribute code is required");
    }

    @Test
    void testValidateMissingPractitionerName() {
        var emptyPerformerName = PerformerNameAndAddress.builder()
            .identifier("identifier")
            .code(HealthcareRegistrationIdentificationCode.CONSULTANT)
            .practitionerName(null)
            .partyName(null)
            .build();

        assertThatThrownBy(emptyPerformerName::validate)
            .isExactlyInstanceOf(EdifactValidationException.class)
            .hasMessage("NAD: Attribute practitionerName is required");
    }

    @Test
    void testValidateMissingPartyName() {
        var emptyPerformingOrganizationName = PerformerNameAndAddress.builder()
            .identifier(null)
            .code(null)
            .practitionerName(null)
            .partyName(null)
            .build();

        assertThatThrownBy(emptyPerformingOrganizationName::validate)
            .isExactlyInstanceOf(EdifactValidationException.class)
            .hasMessage("NAD: Attribute partyName is required");
    }
}
