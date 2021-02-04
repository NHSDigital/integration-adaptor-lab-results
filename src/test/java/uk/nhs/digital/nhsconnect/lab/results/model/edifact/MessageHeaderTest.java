package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import org.junit.jupiter.api.Test;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MessageHeaderTest {

    private static final long SEQUENCE_NUMBER = 3L;

    @Test
    void testToEdifactForValidMessageHeader() {
        final MessageHeader messageHeader = new MessageHeader(3L);

        final String edifact = messageHeader.toEdifact();

        assertEquals("UNH+00000003+FHSREG:0:1:FH:FHS001'", edifact);
    }

    @Test
    void testToEdifactForInvalidMessageHeaderThrowsException() {
        final MessageHeader messageHeader = new MessageHeader();

        final EdifactValidationException exception = assertThrows(EdifactValidationException.class,
                messageHeader::toEdifact);

        assertEquals("UNH: Attribute sequenceNumber is required", exception.getMessage());
    }

    @Test
    void testGetValueForValidMessageHeader() {
        final MessageHeader messageHeader = new MessageHeader(3L);

        final String headerValue = messageHeader.getValue();

        assertEquals("00000003+FHSREG:0:1:FH:FHS001", headerValue);
    }

    @Test
    void testValidateStatefulSequenceNumberNullThrowsException() {
        final MessageHeader messageHeader = new MessageHeader();

        final EdifactValidationException exception = assertThrows(EdifactValidationException.class,
                messageHeader::validateStateful);

        assertEquals("UNH: Attribute sequenceNumber is required", exception.getMessage());
    }

    @Test
    void testValidateStatefulSequenceNumberLessThanMinimumValueThrowsException() {
        final MessageHeader messageHeader = new MessageHeader(0L);

        final EdifactValidationException exception = assertThrows(EdifactValidationException.class,
                messageHeader::validateStateful);

        assertEquals("UNH: Attribute sequenceNumber must be between 1 and 99999999", exception.getMessage());
    }

    @Test
    void testValidateStatefulSequenceNumberMoreThanMaxValueThrowsException() {
        final MessageHeader messageHeader = new MessageHeader(100_000_000L);

        final EdifactValidationException exception = assertThrows(EdifactValidationException.class,
                messageHeader::validateStateful);

        assertEquals("UNH: Attribute sequenceNumber must be between 1 and 99999999", exception.getMessage());
    }

    @Test
    void testValidateStatefulSequenceNumberWithinMinMaxDoesNotThrowException() {
        final MessageHeader messageHeader = new MessageHeader(9_999_999L);

        assertDoesNotThrow(messageHeader::validateStateful);
    }

    @Test
    void testFromStringWithValidEdifactStringReturnsMessageHeader() {
        final MessageHeader messageHeader = MessageHeader.fromString("UNH+00000003+FHSREG:0:1:FH:FHS001");

        assertEquals("UNH", messageHeader.getKey());
        assertEquals("00000003+FHSREG:0:1:FH:FHS001", messageHeader.getValue());
        assertEquals(SEQUENCE_NUMBER, messageHeader.getSequenceNumber());
    }

    @Test
    void testFromStringWithInvalidEdifactStringThrowsException() {
        final IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> MessageHeader.fromString("wrong value"));

        assertEquals("Can't create MessageHeader from wrong value", exception.getMessage());
    }

}
