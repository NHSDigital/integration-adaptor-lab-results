package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;

class PerformerNameAndAddressTest {

    private final PerformerNameAndAddress performingOrganizationNameAndAddress = PerformerNameAndAddress.builder()
        .organizationName("ST JAMES'S UNIVERSITY HOSPITAL")
        .build();

    private final PerformerNameAndAddress performerNameAndAddress = PerformerNameAndAddress.builder()
        .identifier("A2442389")
        .code(HealthcareRegistrationIdentificationCode.CONSULTANT)
        .practitionerName("DR J SMITH")
        .build();

    @Test
    void when_edifactStringDoesNotStartWithCorrectKey_expect_illegalArgumentExceptionIsThrown() {
        assertThatThrownBy(() -> PerformerNameAndAddress.fromString("wrong value"))
            .isExactlyInstanceOf(IllegalArgumentException.class)
            .hasMessage("Can't create PerformerNameAndAddress from wrong value");
    }

    @Test
    void when_edifactStringIsPassed_expect_returnAPerformingOrganizationNameAndAddressObject() {
        assertThat(performingOrganizationNameAndAddress)
            .usingRecursiveComparison()
            .isEqualTo(PerformerNameAndAddress.fromString("NAD+SLA+++ST JAMES?'S UNIVERSITY HOSPITAL"));
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
            .organizationName(null)
            .practitionerName(null);

        assertThatThrownBy(() -> builder.code(HealthcareRegistrationIdentificationCode.fromCode("")))
            .isExactlyInstanceOf(IllegalArgumentException.class)
            .hasMessage("No HealthcareRegistrationIdentificationCode for ''");

    }

    @Test
    void testGetKey() {
        assertThat(performingOrganizationNameAndAddress.getKey()).isEqualTo("NAD");
    }

    @Test
    void testGetPerformingOrganizationName() {
        assertThat(performingOrganizationNameAndAddress.getOrganizationName())
            .isEqualTo("ST JAMES'S UNIVERSITY HOSPITAL");
    }

    @Test
    void testGetPractitionerName() {
        assertThat(performerNameAndAddress.getPractitionerName()).isEqualTo("DR J SMITH");
    }

    @Test
    void testValidateMissingOrganizationName() {
        var emptyPerformingOrganizationName = PerformerNameAndAddress.builder()
            .identifier(null)
            .code(null)
            .practitionerName(null)
            .organizationName(null)
            .build();

        assertThatThrownBy(emptyPerformingOrganizationName::validate)
            .isExactlyInstanceOf(EdifactValidationException.class)
            .hasMessage("NAD: Attribute organizationName is required");
    }

    @Test
    void testValidateMissingCode() {
        var emptyCode = PerformerNameAndAddress.builder()
            .identifier("identifier")
            .code(null)
            .practitionerName("DR J SMITH")
            .organizationName(null)
            .build();

        assertThatThrownBy(emptyCode::validate)
            .isExactlyInstanceOf(EdifactValidationException.class)
            .hasMessage("NAD: Attribute code is required");
    }

    @Test
    void testValidateMissingPerformerName() {
        var emptyPerformerName = PerformerNameAndAddress.builder()
            .identifier("identifier")
            .code(HealthcareRegistrationIdentificationCode.CONSULTANT)
            .practitionerName(null)
            .organizationName(null)
            .build();

        assertThatThrownBy(emptyPerformerName::validate)
            .isExactlyInstanceOf(EdifactValidationException.class)
            .hasMessage("NAD: Attribute practitionerName is required");
    }
}
