package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import org.junit.jupiter.api.Test;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SuppressWarnings("checkstyle:MagicNumber")
class MessageHeaderTest {

    @Test
    void testValidateSequenceNumberNullThrowsException() {
        final var messageHeader = new MessageHeader();

        assertThatThrownBy(messageHeader::validate)
            .isExactlyInstanceOf(EdifactValidationException.class)
            .hasMessage("UNH: Attribute sequenceNumber is required");
    }

    @Test
    void testValidateSequenceNumberLessThanMinimumValueThrowsException() {
        final var messageHeader = new MessageHeader(0L, "");

        assertThatThrownBy(messageHeader::validate)
            .isExactlyInstanceOf(EdifactValidationException.class)
            .hasMessage("UNH: Attribute sequenceNumber must be between 1 and 99999999");
    }

    @Test
    void testValidateSequenceNumberMoreThanMaxValueThrowsException() {
        final var messageHeader = new MessageHeader(100_000_000L, "");

        assertThatThrownBy(messageHeader::validate)
            .isExactlyInstanceOf(EdifactValidationException.class)
            .hasMessage("UNH: Attribute sequenceNumber must be between 1 and 99999999");
    }

    @Test
    void testValidateNoMessageTypeThrowsException() {
        final var messageHeader = new MessageHeader(1L, null);

        assertThatThrownBy(messageHeader::validate)
            .isExactlyInstanceOf(EdifactValidationException.class)
            .hasMessage("UNH: Attribute messageType is required");
    }

    @Test
    void testValidateBlankMessageTypeThrowsException() {
        final var messageHeader = new MessageHeader(1L, " ");

        assertThatThrownBy(messageHeader::validate)
            .isExactlyInstanceOf(EdifactValidationException.class)
            .hasMessage("UNH: Attribute messageType is required");
    }

    @Test
    void testValidateSequenceNumberWithValidValuesDoesNotThrowException() {
        final var messageHeader = new MessageHeader(9_999_999L, "TEST");

        assertDoesNotThrow(messageHeader::validate);
    }

    @Test
    void testFromStringWithValidEdifactStringReturnsMessageHeader() {
        final var messageHeader = MessageHeader.fromString("UNH+00000003+FHSREG:0:1:FH:FHS001");

        assertAll(
            () -> assertThat(messageHeader.getSequenceNumber()).isEqualTo(3),
            () -> assertThat(messageHeader.getMessageType()).isEqualTo("FHS001")
        );
    }

    @Test
    void testFromStringWithInvalidEdifactStringThrowsException() {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> MessageHeader.fromString("wrong value"))
            .withMessage("Can't create MessageHeader from wrong value");
    }
}
