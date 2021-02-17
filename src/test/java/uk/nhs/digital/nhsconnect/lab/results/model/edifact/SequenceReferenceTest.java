package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import org.junit.jupiter.api.Test;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class SequenceReferenceTest {

    @Test
    void testGetKey() {
        final var reference = new SequenceReference(SequenceReferenceTarget.SPECIMEN, "");
        assertThat(reference.getKey()).isEqualTo("RFF");
    }

    @Test
    void testGetValue() {
        final var reference = new SequenceReference(SequenceReferenceTarget.INVESTIGATION, "1A2B3C");
        assertThat(reference.getValue()).isEqualTo("ARL:1A2B3C");
    }

    @Test
    void testFromStringWrongKey() {
        assertThatThrownBy(() -> SequenceReference.fromString("WRONG+ARL:1A2B3C"))
            .isExactlyInstanceOf(IllegalArgumentException.class)
            .hasMessage("Can't create SequenceReference from WRONG+ARL:1A2B3C");
    }

    @Test
    void testFromStringUnrecognisedTarget() {
        assertThatThrownBy(() -> SequenceReference.fromString("RFF+ZZZ:1A2B3C"))
            .isExactlyInstanceOf(IllegalArgumentException.class)
            .hasMessage("No sequenceReferenceTarget qualifier for 'ZZZ'");
    }

    @Test
    void testFromStringAllValues() {
        final var reference = SequenceReference.fromString("RFF+ASL:1A2B3C");
        assertAll(
            () -> assertThat(reference.getTarget()).isEqualTo(SequenceReferenceTarget.SPECIMEN),
            () -> assertThat(reference.getNumber()).isEqualTo("1A2B3C")
        );
    }

    @Test
    void testValidationPasses() {
        final var reference = new SequenceReference(SequenceReferenceTarget.SPECIMEN, "9");
        assertAll(
            () -> assertDoesNotThrow(reference::validateStateful),
            () -> assertDoesNotThrow(reference::preValidate)
        );
    }

    @Test
    void testValidationBlankNumber() {
        final var reference = new SequenceReference(SequenceReferenceTarget.SPECIMEN, "");
        assertAll(
            () -> assertDoesNotThrow(reference::validateStateful),
            () -> assertThatThrownBy(reference::preValidate)
                .isExactlyInstanceOf(EdifactValidationException.class)
                .hasMessage("RFF: attribute number must be an alphanumeric string of up to 6 characters")
        );
    }

    @Test
    void testValidationTooLongNumber() {
        final var reference = new SequenceReference(SequenceReferenceTarget.SPECIMEN, "Qwertyuiop");
        assertAll(
            () -> assertDoesNotThrow(reference::validateStateful),
            () -> assertThatThrownBy(reference::preValidate)
                .isExactlyInstanceOf(EdifactValidationException.class)
                .hasMessage("RFF: attribute number must be an alphanumeric string of up to 6 characters")
        );
    }

    @Test
    void testValidationNonAlphanumericNumber() {
        final var reference = new SequenceReference(SequenceReferenceTarget.SPECIMEN, "失 🤯");
        assertAll(
            () -> assertDoesNotThrow(reference::validateStateful),
            () -> assertThatThrownBy(reference::preValidate)
                .isExactlyInstanceOf(EdifactValidationException.class)
                .hasMessage("RFF: attribute number must be an alphanumeric string of up to 6 characters")
        );
    }
}
