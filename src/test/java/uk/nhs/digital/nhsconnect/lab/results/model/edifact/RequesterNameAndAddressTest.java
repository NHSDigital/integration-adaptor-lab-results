package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import org.junit.jupiter.api.Test;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RequesterNameAndAddressTest {

    private final RequesterNameAndAddress requester = RequesterNameAndAddress.builder()
        .identifier("ABC")
        .code(HealthcareRegistrationIdentificationCode.GP)
        .practitionerName("SMITH")
        .organizationName(null)
        .build();

    private final RequesterNameAndAddress requestingOrganization = RequesterNameAndAddress.builder()
        .identifier(null)
        .code(null)
        .practitionerName(null)
        .organizationName("Matthew's GP")
        .build();

    @Test
    void when_edifactStringDoesNotStartWithRequesterNameAndAddressKey_expect_illegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> RequesterNameAndAddress.fromString("wrong value"));
    }

    @Test
    void when_edifactStringIsPassed_expect_returnARequesterObject() {
        assertThat(requester)
            .usingRecursiveComparison()
            .isEqualTo(RequesterNameAndAddress.fromString("NAD+PO+ABC:900++SMITH"));
    }

    @Test
    void when_edifactStringIsPassed_expect_returnARequestingOrganizationObject() {
        assertThat(requestingOrganization)
            .usingRecursiveComparison()
            .isEqualTo(RequesterNameAndAddress.fromString("NAD+PO+++Matthew?'s GP"));
    }

    @Test
    void when_parsingEdifactStringToRequesterObject_expect_returnCorrectRequesterObject() {
        String edifactString = "NAD+PO+ABC:900++SMITH";

        var requesterResult = RequesterNameAndAddress.fromString(edifactString);

        assertThat(requesterResult.getIdentifier()).isEqualTo("ABC");
        assertThat(requesterResult.getPractitionerName()).isEqualTo("SMITH");
        assertThat(requesterResult.getCode())
            .isEqualTo(HealthcareRegistrationIdentificationCode.GP);
        assertNull(requesterResult.getOrganizationName());
    }

    @Test
    void when_parsingEdifactStringToRequestingOrganizationObject_expect_returnCorrectRequestingOrganizationObject() {
        String edifactString = "NAD+PO+++Matthew?'s GP";

        var requestingOrganizationResult = RequesterNameAndAddress.fromString(edifactString);

        assertNull(requestingOrganizationResult.getIdentifier());
        assertNull(requestingOrganizationResult.getPractitionerName());
        assertNull(requestingOrganizationResult.getCode());
        assertThat(requestingOrganizationResult.getOrganizationName())
            .isEqualTo("Matthew's GP");
    }

    @Test
    void testGetKey() {
        assertEquals(requester.getKey(), "NAD");
    }

    @Test
    void testValidateDoesNotThrowException() {
        assertDoesNotThrow(requester::validate);
        assertDoesNotThrow(requestingOrganization::validate);
    }

    @Test
    void testValidateMissingIdentifier() {
        RequesterNameAndAddress emptyIdentifier = RequesterNameAndAddress.builder()
            .identifier(null)
            .code(HealthcareRegistrationIdentificationCode.GP)
            .practitionerName("SMITH")
            .organizationName(null)
            .build();

        assertThatThrownBy(emptyIdentifier::validate)
            .isExactlyInstanceOf(EdifactValidationException.class)
            .hasMessage("NAD: Attribute identifier is required");
    }

    @Test
    void testValidateMissingCode() {
        RequesterNameAndAddress emptyCode = RequesterNameAndAddress.builder()
            .identifier("ABC")
            .code(null)
            .practitionerName("SMITH")
            .organizationName(null)
            .build();

        assertThatThrownBy(emptyCode::validate)
            .isExactlyInstanceOf(EdifactValidationException.class)
            .hasMessage("NAD: Attribute code is required");
    }

    @Test
    void testValidateMissingPractitionerName() {
        RequesterNameAndAddress emptyRequesterName = RequesterNameAndAddress.builder()
            .identifier("ABC")
            .code(HealthcareRegistrationIdentificationCode.GP)
            .practitionerName(null)
            .organizationName(null)
            .build();

        assertThatThrownBy(emptyRequesterName::validate)
            .isExactlyInstanceOf(EdifactValidationException.class)
            .hasMessage("NAD: Attribute practitionerName is required");
    }

    @Test
    void testValidateMissingOrganizationName() {
        RequesterNameAndAddress emptyRequestingOrganizationName = RequesterNameAndAddress.builder()
            .identifier(null)
            .code(null)
            .practitionerName(null)
            .organizationName(null)
            .build();

        assertThatThrownBy(emptyRequestingOrganizationName::validate)
            .isExactlyInstanceOf(EdifactValidationException.class)
            .hasMessage("NAD: Attribute organizationName is required");
    }
}
