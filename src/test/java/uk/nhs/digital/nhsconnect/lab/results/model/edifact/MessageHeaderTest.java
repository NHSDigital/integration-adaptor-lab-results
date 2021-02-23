package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import org.junit.jupiter.api.Test;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MessageHeaderTest {

    private static final long SEQUENCE_NUMBER = 3L;

    @Test
    void testValidateSequenceNumberNullThrowsException() {
        final MessageHeader messageHeader = new MessageHeader();

        final EdifactValidationException exception = assertThrows(EdifactValidationException.class,
                messageHeader::validate);

        assertEquals("UNH: Attribute sequenceNumber is required", exception.getMessage());
    }

    @Test
    void testValidateSequenceNumberLessThanMinimumValueThrowsException() {
        final MessageHeader messageHeader = new MessageHeader(0L);

        final EdifactValidationException exception = assertThrows(EdifactValidationException.class,
                messageHeader::validate);

        assertEquals("UNH: Attribute sequenceNumber must be between 1 and 99999999", exception.getMessage());
    }

    @Test
    void testValidateSequenceNumberMoreThanMaxValueThrowsException() {
        final MessageHeader messageHeader = new MessageHeader(100_000_000L);

        final EdifactValidationException exception = assertThrows(EdifactValidationException.class,
                messageHeader::validate);

        assertEquals("UNH: Attribute sequenceNumber must be between 1 and 99999999", exception.getMessage());
    }

    @Test
    void testValidateSequenceNumberWithinMinMaxDoesNotThrowException() {
        final MessageHeader messageHeader = new MessageHeader(9_999_999L);

        assertDoesNotThrow(messageHeader::validate);
    }

    @Test
    void testFromStringWithValidEdifactStringReturnsMessageHeader() {
        final MessageHeader messageHeader = MessageHeader.fromString("UNH+00000003+FHSREG:0:1:FH:FHS001");

        assertEquals("UNH", messageHeader.getKey());
        assertEquals(SEQUENCE_NUMBER, messageHeader.getSequenceNumber());
    }

    @Test
    void testFromStringWithInvalidEdifactStringThrowsException() {
        final IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> MessageHeader.fromString("wrong value"));

        assertEquals("Can't create MessageHeader from wrong value", exception.getMessage());
    }
}
