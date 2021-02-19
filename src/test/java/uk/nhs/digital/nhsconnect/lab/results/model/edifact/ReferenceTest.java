package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import org.junit.jupiter.api.Test;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class ReferenceTest {

    @Test
    void testGetKey() {
        final var reference = new Reference(ReferenceType.SPECIMEN, "");
        assertThat(reference.getKey()).isEqualTo("RFF");
    }

    @Test
    void testGetValue() {
        final var reference = new Reference(ReferenceType.INVESTIGATION, "1A2B3C");
        assertThat(reference.getValue()).isEqualTo("ARL:1A2B3C");
    }

    @Test
    void testFromStringWrongKey() {
        assertThatThrownBy(() -> Reference.fromString("WRONG+ARL:1A2B3C"))
            .isExactlyInstanceOf(IllegalArgumentException.class)
            .hasMessage("Can't create Reference from WRONG+ARL:1A2B3C");
    }

    @Test
    void testFromStringUnrecognisedTarget() {
        assertThatThrownBy(() -> Reference.fromString("RFF+ZZZ:1A2B3C"))
            .isExactlyInstanceOf(IllegalArgumentException.class)
            .hasMessage("No reference qualifier for 'ZZZ'");
    }

    @Test
    void testFromStringAllValues() {
        final var reference = Reference.fromString("RFF+ASL:1A2B3C");
        assertAll(
            () -> assertThat(reference.getTarget()).isEqualTo(ReferenceType.SPECIMEN),
            () -> assertThat(reference.getNumber()).isEqualTo("1A2B3C")
        );
    }

    @Test
    void testValidationPasses() {
        final var reference = new Reference(ReferenceType.SPECIMEN, "9");
        assertAll(
            () -> assertDoesNotThrow(reference::validateStateful),
            () -> assertDoesNotThrow(reference::preValidate)
        );
    }

    @Test
    void testValidationEmptyNumber() {
        final var reference = new Reference(ReferenceType.SPECIMEN, "");
        assertAll(
            () -> assertDoesNotThrow(reference::validateStateful),
            () -> assertThatThrownBy(reference::preValidate)
                .isExactlyInstanceOf(EdifactValidationException.class)
                .hasMessage("RFF: attribute number must be an alphanumeric string of up to 6 characters")
        );
    }

    @Test
    void testValidationTooLongNumber() {
        final var reference = new Reference(ReferenceType.SPECIMEN, "Qwertyuiop");
        assertAll(
            () -> assertDoesNotThrow(reference::validateStateful),
            () -> assertThatThrownBy(reference::preValidate)
                .isExactlyInstanceOf(EdifactValidationException.class)
                .hasMessage("RFF: attribute number must be an alphanumeric string of up to 6 characters")
        );
    }

    @Test
    void testValidationNonAlphanumericNumber() {
        final var reference = new Reference(ReferenceType.SPECIMEN, "失 🤯");
        assertAll(
            () -> assertDoesNotThrow(reference::validateStateful),
            () -> assertThatThrownBy(reference::preValidate)
                .isExactlyInstanceOf(EdifactValidationException.class)
                .hasMessage("RFF: attribute number must be an alphanumeric string of up to 6 characters")
        );
    }
}
