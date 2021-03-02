package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import org.junit.jupiter.api.Test;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RequesterNameAndAddressTest {

    private final RequesterNameAndAddress requesterNameAndAddress = new RequesterNameAndAddress(
            "ABC", HealthcareRegistrationIdentificationCode.GP, "SMITH"
    );

    @Test
    void when_edifactStringDoesNotStartWithRequesterNameAndAddressKey_expect_illegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> RequesterNameAndAddress.fromString("wrong value"));
    }

    @Test
    void when_edifactStringIsPassed_expect_returnARequesterNameAndAddressObject() {
        assertThat(requesterNameAndAddress)
                .usingRecursiveComparison()
                .isEqualTo(RequesterNameAndAddress.fromString("NAD+PO+ABC:900++SMITH"));
    }

    @Test
    void when_mappingSegmentObjectToEdifactString_expect_returnCorrectEdifactString() {
        String edifactString = "NAD+PO+ABC:900++SMITH";

        RequesterNameAndAddress requester = RequesterNameAndAddress.builder()
                .identifier("ABC")
                .healthcareRegistrationIdentificationCode(HealthcareRegistrationIdentificationCode.GP)
                .requesterName("SMITH")
                .build();

        var fromString = RequesterNameAndAddress.fromString(edifactString);

        assertThat(fromString.getIdentifier()).isEqualTo("ABC");
        assertThat(fromString.getRequesterName()).isEqualTo("SMITH");
        assertThat(fromString.getHealthcareRegistrationIdentificationCode())
                .isEqualTo(HealthcareRegistrationIdentificationCode.GP);
    }

    @Test
    void when_mappingSegmentObjectToEdifactStringWithEmptyIdentifierField_expect_edifactValidationException() {
        RequesterNameAndAddress requester = RequesterNameAndAddress.builder()
                .identifier("")
                .healthcareRegistrationIdentificationCode(HealthcareRegistrationIdentificationCode.GP)
                .requesterName("SMITH")
                .build();

        assertThrows(EdifactValidationException.class, requester::validate);
    }

    @Test
    void when_mappingSegmentObjectToEdifactStringWithEmptyRequesterNameField_expect_edifactValidationException() {
        RequesterNameAndAddress requester = RequesterNameAndAddress.builder()
                .identifier("ABC")
                .healthcareRegistrationIdentificationCode(HealthcareRegistrationIdentificationCode.GP)
                .requesterName("")
                .build();

        assertThrows(EdifactValidationException.class, requester::validate);
    }

    @Test
    void when_buildingSegmentObjectWithoutMandatoryFields_expect_nullPointerException() {
        assertThrows(NullPointerException.class, () -> RequesterNameAndAddress.builder().build());
    }

    @Test
    void testGetKey() {
        assertEquals(requesterNameAndAddress.getKey(), "NAD");
    }

    @Test
    void testValidate() {
        assertDoesNotThrow(requesterNameAndAddress::validate);

        RequesterNameAndAddress emptyIdentifier = new RequesterNameAndAddress(
                "", HealthcareRegistrationIdentificationCode.GP, "SMITH"
        );
        RequesterNameAndAddress emptyRequesterName = new RequesterNameAndAddress(
                "ABC", HealthcareRegistrationIdentificationCode.GP, ""
        );

        assertAll(
                () -> assertThatThrownBy(emptyIdentifier::validate)
                        .isExactlyInstanceOf(EdifactValidationException.class)
                        .hasMessage("NAD: Attribute identifier is required"),
                () -> assertThatThrownBy(emptyRequesterName::validate)
                        .isExactlyInstanceOf(EdifactValidationException.class)
                        .hasMessage("NAD: Attribute requesterName is required")
        );
    }
}
