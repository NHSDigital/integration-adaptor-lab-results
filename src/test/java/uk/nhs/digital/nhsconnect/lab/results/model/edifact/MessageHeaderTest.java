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
        final var messageHeader = MessageHeader.builder().build();

        assertThatThrownBy(messageHeader::validate)
            .isExactlyInstanceOf(EdifactValidationException.class)
            .hasMessage("UNH: Attribute sequenceNumber is required");
    }

    @Test
    void testValidateSequenceNumberLessThanMinimumValueThrowsException() {
        final var messageHeader = MessageHeader.builder()
            .sequenceNumber(0L)
            .messageType(MessageType.PATHOLOGY)
            .build();

        assertThatThrownBy(messageHeader::validate)
            .isExactlyInstanceOf(EdifactValidationException.class)
            .hasMessage("UNH: Attribute sequenceNumber must be between 1 and 99999999");
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    @Test
    void testValidateSequenceNumberMoreThanMaxValueThrowsException() {
        final var messageHeader = MessageHeader.builder()
            .sequenceNumber(100_000_000L)
            .messageType(MessageType.PATHOLOGY)
            .build();

        assertThatThrownBy(messageHeader::validate)
            .isExactlyInstanceOf(EdifactValidationException.class)
            .hasMessage("UNH: Attribute sequenceNumber must be between 1 and 99999999");
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    @Test
    void testValidateNoMessageTypeThrowsException() {
        final var messageHeader = MessageHeader.builder()
            .sequenceNumber(1L)
            .messageType(null)
            .build();

        assertThatThrownBy(messageHeader::validate)
            .isExactlyInstanceOf(EdifactValidationException.class)
            .hasMessage("UNH: Attribute messageType is required");
    }

    @Test
    void testValidateSequenceNumberWithValidValuesDoesNotThrowException() {
        final var messageHeader = MessageHeader.builder()
            .sequenceNumber(9_999_999L)
            .messageType(MessageType.PATHOLOGY)
            .build();

        assertDoesNotThrow(messageHeader::validate);
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    @Test
    void testFromStringWithValidEdifactStringReturnsMessageHeader() {
        final var messageHeader = MessageHeader.fromString("UNH+00000003+MEDRPT:0:1:RT:NHS003");

        assertAll(
            () -> assertThat(messageHeader.getSequenceNumber()).isEqualTo(3),
            () -> assertThat(messageHeader.getMessageType()).isEqualTo(MessageType.PATHOLOGY)
        );
    }

    @Test
    void testFromStringWithInvalidEdifactStringThrowsException() {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> MessageHeader.fromString("wrong value"))
            .withMessage("Can't create MessageHeader from wrong value");
    }
}
