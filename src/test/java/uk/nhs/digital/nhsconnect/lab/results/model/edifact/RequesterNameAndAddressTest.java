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

    private final RequesterNameAndAddress requesterNameAndAddress = RequesterNameAndAddress.builder()
        .identifier("ABC")
        .code(HealthcareRegistrationIdentificationCode.GP)
        .name("SMITH")
        .build();

    @Test
    void when_edifactStringDoesNotStartWithRequesterNameAndAddressKey_expect_illegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> RequesterNameAndAddress.fromString("wrong value"));
    }

    @Test
    void when_edifactStringIsPassed_expect_returnARequesterObject() {
        assertThat(RequesterNameAndAddress.fromString("NAD+PO+ABC:900++SMITH"))
            .usingRecursiveComparison()
            .isEqualTo(requesterNameAndAddress);
    }

    @Test
    void when_parsingEdifactStringToRequesterObject_expect_returnCorrectRequesterObject() {
        String edifactString = "NAD+PO+ABC:900++SMITH";

        var requesterResult = RequesterNameAndAddress.fromString(edifactString);

        assertAll(
            () -> assertThat(requesterResult.getIdentifier()).isEqualTo("ABC"),
            () -> assertThat(requesterResult.getName()).isEqualTo("SMITH"),
            () -> assertThat(requesterResult.getCode()).isEqualTo(HealthcareRegistrationIdentificationCode.GP)
        );
    }

    @Test
    void when_parsingEdifactStringToRequesterObjectWithoutIdentifierAndCode_expect_returnCorrectRequesterObject() {
        String edifactString = "NAD+PO+++SMITH";

        var requesterResult = RequesterNameAndAddress.fromString(edifactString);

        assertAll(
            () -> assertThat(requesterResult.getIdentifier()).isNull(),
            () -> assertThat(requesterResult.getName()).isEqualTo("SMITH"),
            () -> assertThat(requesterResult.getCode()).isNull()
        );
    }

    @Test
    void when_parsingEdifactStringToRequesterObjectWithoutIdentifier_expect_returnCorrectRequesterObject() {
        String edifactString = "NAD+PO+:900++SMITH";

        var requesterResult = RequesterNameAndAddress.fromString(edifactString);

        assertAll(
            () -> assertThat(requesterResult.getIdentifier()).isNull(),
            () -> assertThat(requesterResult.getName()).isEqualTo("SMITH"),
            () -> assertThat(requesterResult.getCode()).isNull()
        );
    }

    @Test
    void when_parsingEdifactStringToRequesterObjectWithoutCode_expect_returnCorrectRequesterObject() {
        String edifactString = "NAD+PO+ABC:++SMITH";

        var requesterResult = RequesterNameAndAddress.fromString(edifactString);

        assertAll(
            () -> assertThat(requesterResult.getIdentifier()).isEqualTo("ABC"),
            () -> assertThat(requesterResult.getName()).isEqualTo("SMITH"),
            () -> assertThat(requesterResult.getCode()).isNull()
        );
    }


    @Test
    void testGetKey() {
        assertEquals(requesterNameAndAddress.getKey(), "NAD");
    }

    @Test
    void testValidateDoesNotThrowException() {
        assertDoesNotThrow(requesterNameAndAddress::validate);
    }

    @Test
    void testValidateMissingName() {
        RequesterNameAndAddress emptyRequesterName = RequesterNameAndAddress.builder()
            .identifier("ABC")
            .code(HealthcareRegistrationIdentificationCode.GP)
            .name(null)
            .build();

        assertThatThrownBy(emptyRequesterName::validate)
            .isExactlyInstanceOf(EdifactValidationException.class)
            .hasMessage("NAD: Attribute name is required");
    }
}
