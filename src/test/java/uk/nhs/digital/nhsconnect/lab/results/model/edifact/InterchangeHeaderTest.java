package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import org.junit.jupiter.api.Test;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InterchangeHeaderTest {

    @Test
    void testValidateSequenceNumberNullThrowsException() {
        final InterchangeHeader interchangeHeader =
                InterchangeHeader.fromString("UNB+UNOA:2+SNDR+RECP+190523:0900");

        final EdifactValidationException exception = assertThrows(EdifactValidationException.class,
                interchangeHeader::validate);

        assertEquals("UNB: Attribute sequenceNumber is required", exception.getMessage());
    }

    @Test
    void testValidateSequenceNumberLessThanOneThrowsException() {
        final InterchangeHeader interchangeHeader =
                InterchangeHeader.fromString("UNB+UNOA:2+SNDR+RECP+190523:0900+00000000");

        final EdifactValidationException exception = assertThrows(EdifactValidationException.class,
                interchangeHeader::validate);

        assertEquals("UNB: Attribute sequenceNumber must be between 1 and 99999999", exception.getMessage());
    }

    @Test
    void testValidateSequenceNumberMoreThanMaxThrowsException() {
        final InterchangeHeader interchangeHeader =
                InterchangeHeader.fromString("UNB+UNOA:2+SNDR+RECP+190523:0900+100000000");

        final EdifactValidationException exception = assertThrows(EdifactValidationException.class,
                interchangeHeader::validate);

        assertEquals("UNB: Attribute sequenceNumber must be between 1 and 99999999", exception.getMessage());
    }

    @Test
    void testValidateSequenceNumberWithinMinMaxDoesNotThrowException() {
        final InterchangeHeader interchangeHeader =
                InterchangeHeader.fromString("UNB+UNOA:2+SNDR+RECP+190523:0900+00000001");

        assertDoesNotThrow(interchangeHeader::validate);
    }

    @Test
    void testValidationEmptySenderThrowsException() {
        final InterchangeHeader interchangeHeader =
                InterchangeHeader.fromString("UNB+UNOA:2++RECP+190523:0900+00000001");

        final EdifactValidationException exception = assertThrows(EdifactValidationException.class,
                interchangeHeader::validate);

        assertEquals("UNB: Attribute sender is required", exception.getMessage());
    }

    @Test
    void testValidationEmptyRecipientThrowsException() {
        final InterchangeHeader interchangeHeader =
                InterchangeHeader.fromString("UNB+UNOA:2+SNDR++190523:0900+00000001");

        final EdifactValidationException exception = assertThrows(EdifactValidationException.class,
                interchangeHeader::validate);

        assertEquals("UNB: Attribute recipient is required", exception.getMessage());
    }

    @Test
    void testFromStringWithValidEdifactStringReturnsInterchangeHeader() {
        final var interchangeHeader = InterchangeHeader.fromString("UNB+UNOA:2+SNDR+RECP+190323:0900+00000001");

        assertEquals(InterchangeHeader.KEY, interchangeHeader.getKey());
        assertEquals("SNDR", interchangeHeader.getSender());
        assertEquals("RECP", interchangeHeader.getRecipient());
        assertEquals(1L, interchangeHeader.getSequenceNumber());
    }

    @Test
    void testFromStringWithInvalidEdifactStringThrowsException() {
        final IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> InterchangeHeader.fromString("wrong value"));

        assertEquals("Can't create InterchangeHeader from wrong value", exception.getMessage());
    }
}
