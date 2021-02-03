package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import org.junit.jupiter.api.Test;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RequesterNameAndAddressTest {

    private final RequesterNameAndAddress requesterNameAndAddress = new RequesterNameAndAddress(
        "ABC", HealthcareRegistrationIdentificationCode.GP, "SMITH"
    );

    @Test
    void when_edifactStringDoesNotStartWithRequesterNameAndAddressKey_expect_illegalArgumentExceptionIsThrown() {
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
        String expectedEdifactString = "NAD+PO+ABC:900++SMITH'";

        RequesterNameAndAddress requester = RequesterNameAndAddress.builder()
            .identifier("ABC")
            .healthcareRegistrationIdentificationCode(HealthcareRegistrationIdentificationCode.GP)
            .requesterName("SMITH")
            .build();

        assertEquals(expectedEdifactString, requester.toEdifact());
    }

    @Test
    void when_mappingSegmentObjectToEdifactStringWithEmptyIdentifierField_expect_edifactValidationExceptionIsThrown() {
        RequesterNameAndAddress requester = RequesterNameAndAddress.builder()
            .identifier("")
            .healthcareRegistrationIdentificationCode(HealthcareRegistrationIdentificationCode.GP)
            .requesterName("SMITH")
            .build();

        assertThrows(EdifactValidationException.class, requester::toEdifact);
    }

    @Test
    void when_mappingSegmentObjectToEdifactStringWithEmptyRequesterNameField_expect_edifactValidationExceptionIsThrown() {
        RequesterNameAndAddress requester = RequesterNameAndAddress.builder()
                .identifier("ABC")
                .healthcareRegistrationIdentificationCode(HealthcareRegistrationIdentificationCode.GP)
                .requesterName("")
                .build();

        assertThrows(EdifactValidationException.class, requester::toEdifact);
    }

    @Test
    void when_buildingSegmentObjectWithoutMandatoryFields_expect_nullPointerExceptionIsThrown() {
        assertThrows(NullPointerException.class, () -> RequesterNameAndAddress.builder().build());
    }

    @Test
    void testGetKey() {
        assertEquals(requesterNameAndAddress.getKey(), "NAD");
    }

    @Test
    void testGetValue() {
        assertEquals(requesterNameAndAddress.getValue(), "PO+ABC:900++SMITH");
    }

    @Test
    void testValidateStateful() {
        assertDoesNotThrow(requesterNameAndAddress::validateStateful);
    }

    @Test
    void testPreValidate() {
        RequesterNameAndAddress emptyIdentifier = new RequesterNameAndAddress(
                "", HealthcareRegistrationIdentificationCode.GP, "SMITH"
        );
        RequesterNameAndAddress emptyRequesterName = new RequesterNameAndAddress(
                "ABC", HealthcareRegistrationIdentificationCode.GP, ""
        );

        assertSoftly(softly -> {
            softly.assertThatThrownBy(emptyIdentifier::preValidate)
                    .isExactlyInstanceOf(EdifactValidationException.class)
                    .hasMessage("NAD: Attribute identifier is required");

            softly.assertThatThrownBy(emptyRequesterName::preValidate)
                    .isExactlyInstanceOf(EdifactValidationException.class)
                    .hasMessage("NAD: Attribute requesterName is required");
        });
    }
}
