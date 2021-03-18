package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import org.junit.jupiter.api.Test;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;
import uk.nhs.digital.nhsconnect.lab.results.model.enums.HealthcareRegistrationIdentificationCode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class PerformerNameAndAddressTest {

    private final PerformerNameAndAddress performerNameAndAddress = PerformerNameAndAddress.builder()
        .identifier("A2442389")
        .code(HealthcareRegistrationIdentificationCode.CONSULTANT)
        .name("DR J SMITH")
        .build();

    @Test
    void when_edifactStringDoesNotStartWithCorrectKey_expect_illegalArgumentExceptionIsThrown() {
        assertThatThrownBy(() -> PerformerNameAndAddress.fromString("wrong value"))
            .isExactlyInstanceOf(IllegalArgumentException.class)
            .hasMessage("Can't create PerformerNameAndAddress from wrong value");
    }

    @Test
    void when_parsingEdifactStringToPerformerObject_expect_returnCorrectPerformerObject() {
        String edifactString = "NAD+SLA+ABC:900++SMITH";

        var performerResult = PerformerNameAndAddress.fromString(edifactString);

        assertAll(
            () -> assertThat(performerResult.getIdentifier()).isEqualTo("ABC"),
            () -> assertThat(performerResult.getName()).isEqualTo("SMITH"),
            () -> assertThat(performerResult.getCode()).isEqualTo(HealthcareRegistrationIdentificationCode.GP)
        );
    }

    @Test
    void when_parsingEdifactStringToPerformerObjectWithoutIdentifierAndCode_expect_returnCorrectPerformerObject() {
        String edifactString = "NAD+SLA+++SMITH";

        var performerResult = PerformerNameAndAddress.fromString(edifactString);

        assertAll(
            () -> assertThat(performerResult.getIdentifier()).isNull(),
            () -> assertThat(performerResult.getName()).isEqualTo("SMITH"),
            () -> assertThat(performerResult.getCode()).isNull()
        );
    }

    @Test
    void when_parsingEdifactStringToRequesterObjectWithoutIdentifier_expect_returnCorrectRequesterObject() {
        String edifactString = "NAD+SLA+:900++SMITH";

        var performerResult = PerformerNameAndAddress.fromString(edifactString);

        assertAll(
            () -> assertThat(performerResult.getIdentifier()).isNull(),
            () -> assertThat(performerResult.getName()).isEqualTo("SMITH"),
            () -> assertThat(performerResult.getCode()).isNull()
        );
    }

    @Test
    void when_parsingEdifactStringToRequesterObjectWithoutCode_expect_returnCorrectRequesterObject() {
        String edifactString = "NAD+SLA+ABC:++SMITH";

        var performerResult = PerformerNameAndAddress.fromString(edifactString);

        assertAll(
            () -> assertThat(performerResult.getIdentifier()).isEqualTo("ABC"),
            () -> assertThat(performerResult.getName()).isEqualTo("SMITH"),
            () -> assertThat(performerResult.getCode()).isNull()
        );
    }

    @Test
    void when_parsingEdifactStringToRequesterObjectWithoutName_expect_returnCorrectRequesterObject() {
        String edifactString = "NAD+SLA+ABC:900";

        var performerResult = PerformerNameAndAddress.fromString(edifactString);

        assertAll(
            () -> assertThat(performerResult.getIdentifier()).isEqualTo("ABC"),
            () -> assertThat(performerResult.getName()).isNull(),
            () -> assertThat(performerResult.getCode()).isEqualTo(HealthcareRegistrationIdentificationCode.GP)
        );
    }

    @Test
    void testGetKey() {
        assertThat(performerNameAndAddress.getKey()).isEqualTo("NAD");
    }

    @Test
    void testValidateDoesNotThrowException() {
        assertDoesNotThrow(performerNameAndAddress::validate);
    }

    @Test
    void testValidateMissingIdentifierAndName() {
        var emptyCode = PerformerNameAndAddress.builder()
            .identifier(null)
            .code(null)
            .name(null)
            .build();

        assertThatThrownBy(emptyCode::validate)
            .isExactlyInstanceOf(EdifactValidationException.class)
            .hasMessage("NAD: Attribute identifier or name is required");
    }
}
