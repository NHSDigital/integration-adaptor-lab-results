package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import org.junit.jupiter.api.Test;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RequesterNameAndAddressTest {

    private final RequesterNameAndAddress requester = RequesterNameAndAddress.builder()
            .identifier("ABC")
            .healthcareRegistrationIdentificationCode(HealthcareRegistrationIdentificationCode.GP)
            .requesterName("SMITH")
            .requestingOrganizationName(null)
            .build();

    private final RequesterNameAndAddress requestingOrganization = RequesterNameAndAddress.builder()
            .identifier(null)
            .healthcareRegistrationIdentificationCode(null)
            .requesterName(null)
            .requestingOrganizationName("A GP")
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
                .isEqualTo(RequesterNameAndAddress.fromString("NAD+PO+++A GP"));
    }

    @Test
    void when_parsingEdifactStringToRequesterObject_expect_returnCorrectRequesterObject() {
        String edifactString = "NAD+PO+ABC:900++SMITH";

        var requesterResult = RequesterNameAndAddress.fromString(edifactString);

        assertThat(requesterResult.getIdentifier()).isEqualTo("ABC");
        assertThat(requesterResult.getRequesterName()).isEqualTo("SMITH");
        assertThat(requesterResult.getHealthcareRegistrationIdentificationCode())
            .isEqualTo(HealthcareRegistrationIdentificationCode.GP);
        assertNull(requesterResult.getRequestingOrganizationName());
    }

    @Test
    void when_parsingEdifactStringToRequestingOrganizationObject_expect_returnCorrectRequestingOrganizationObject() {
        String edifactString = "NAD+PO+++A GP";

        var requestingOrganizationResult = RequesterNameAndAddress.fromString(edifactString);

        assertNull(requestingOrganizationResult.getIdentifier());
        assertNull(requestingOrganizationResult.getRequesterName());
        assertNull(requestingOrganizationResult.getHealthcareRegistrationIdentificationCode());
        assertThat(requestingOrganizationResult.getRequestingOrganizationName())
            .isEqualTo("A GP");
    }

    @Test
    void testGetKey() {
        assertEquals(requester.getKey(), "NAD");
    }

    @Test
    void testValidate() {
        assertDoesNotThrow(requester::validate);
        assertDoesNotThrow(requestingOrganization::validate);

        RequesterNameAndAddress emptyIdentifier = RequesterNameAndAddress.builder()
            .identifier(null)
            .healthcareRegistrationIdentificationCode(HealthcareRegistrationIdentificationCode.GP)
            .requesterName("SMITH")
            .requestingOrganizationName(null)
            .build();

        RequesterNameAndAddress emptyCode = RequesterNameAndAddress.builder()
                .identifier("ABC")
                .healthcareRegistrationIdentificationCode(null)
                .requesterName("SMITH")
                .requestingOrganizationName(null)
                .build();

        RequesterNameAndAddress emptyRequesterName = RequesterNameAndAddress.builder()
                .identifier("ABC")
                .healthcareRegistrationIdentificationCode(HealthcareRegistrationIdentificationCode.GP)
                .requesterName(null)
                .requestingOrganizationName(null)
                .build();

        RequesterNameAndAddress emptyRequestingOrganizationName = RequesterNameAndAddress.builder()
                .identifier(null)
                .healthcareRegistrationIdentificationCode(null)
                .requesterName(null)
                .requestingOrganizationName(null)
                .build();

        assertAll(
            () -> assertThatThrownBy(emptyIdentifier::validate)
                .isExactlyInstanceOf(EdifactValidationException.class)
                .hasMessage("NAD: Attribute identifier is required"),
            () -> assertThatThrownBy(emptyCode::validate)
                .isExactlyInstanceOf(EdifactValidationException.class)
                .hasMessage("NAD: Attribute healthcareRegistrationIdentificationCode is required"),
            () -> assertThatThrownBy(emptyRequesterName::validate)
                .isExactlyInstanceOf(EdifactValidationException.class)
                .hasMessage("NAD: Attribute requesterName is required"),
            () -> assertThatThrownBy(emptyRequestingOrganizationName::validate)
                .isExactlyInstanceOf(EdifactValidationException.class)
                .hasMessage("NAD: Attribute requestingOrganizationName is required")
        );
    }
}
