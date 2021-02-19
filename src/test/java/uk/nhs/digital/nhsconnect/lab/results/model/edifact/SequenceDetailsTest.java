package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import org.junit.jupiter.api.Test;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class SequenceDetailsTest {
    @Test
    void testGetKey() {
        final var sequence = new SequenceDetails("");
        assertThat(sequence.getKey()).isEqualTo("SEQ");
    }

    @Test
    void testGetValue() {
        final var sequence = new SequenceDetails("1A2B3C");
        assertThat(sequence.getValue()).isEqualTo("1A2B3C");
    }

    @Test
    void testFromStringWrongKey() {
        assertThatThrownBy(() -> SequenceDetails.fromString("WRONG++123"))
            .isExactlyInstanceOf(IllegalArgumentException.class)
            .hasMessage("Can't create SequenceDetails from WRONG++123");
    }

    @Test
    void testFromStringAllValues() {
        final var sequence = SequenceDetails.fromString("SEQ++0");
        assertThat(sequence.getNumber()).isEqualTo("0");
    }

    @Test
    void testFromStringInsufficientParts() {
        assertThatThrownBy(() -> SequenceDetails.fromString("SEQ+1"))
            .isExactlyInstanceOf(ArrayIndexOutOfBoundsException.class);
    }

    @Test
    void testValidationPasses() {
        final var sequence = new SequenceDetails("123456");
        assertAll(
            () -> assertDoesNotThrow(sequence::validateStateful),
            () -> assertDoesNotThrow(sequence::preValidate)
        );
    }

    @Test
    void testValidationEmptyNumber() {
        final var sequence = new SequenceDetails("");
        assertAll(
            () -> assertDoesNotThrow(sequence::validateStateful),
            () -> assertThatThrownBy(sequence::preValidate)
                .isExactlyInstanceOf(EdifactValidationException.class)
                .hasMessage("SEQ: attribute number must be an alphanumeric string of up to 6 characters")
        );
    }

    @Test
    void testValidationNonAlphaNumericNumber() {
        final var sequence = new SequenceDetails("1.00");
        assertAll(
            () -> assertDoesNotThrow(sequence::validateStateful),
            () -> assertThatThrownBy(sequence::preValidate)
                .isExactlyInstanceOf(EdifactValidationException.class)
                .hasMessage("SEQ: attribute number must be an alphanumeric string of up to 6 characters")
        );
    }

    @Test
    void testValidationTooLongNumber() {
        final var sequence = new SequenceDetails("ABCDEFG");
        assertAll(
            () -> assertDoesNotThrow(sequence::validateStateful),
            () -> assertThatThrownBy(sequence::preValidate)
                .isExactlyInstanceOf(EdifactValidationException.class)
                .hasMessage("SEQ: attribute number must be an alphanumeric string of up to 6 characters")
        );

    }
}
