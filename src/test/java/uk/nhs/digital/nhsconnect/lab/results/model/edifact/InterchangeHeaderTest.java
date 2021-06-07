package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import org.junit.jupiter.api.Test;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertAll;

class InterchangeHeaderTest {

    @Test
    void testValidateSequenceNumberNullThrowsException() {
        final InterchangeHeader interchangeHeader =
            InterchangeHeader.fromString("UNB+UNOA:2+SNDR:80+RECP:80+190523:0900");

        final EdifactValidationException exception = assertThrows(EdifactValidationException.class,
            interchangeHeader::validate);

        assertEquals("UNB: Attribute sequenceNumber is required", exception.getMessage());
    }

    @Test
    void testValidateSequenceNumberLessThanOneThrowsException() {
        final InterchangeHeader interchangeHeader =
            InterchangeHeader.fromString("UNB+UNOA:2+SNDR:80+RECP:80+190523:0900+00000000");

        final EdifactValidationException exception = assertThrows(EdifactValidationException.class,
            interchangeHeader::validate);

        assertEquals("UNB: Attribute sequenceNumber must be between 1 and 99999999", exception.getMessage());
    }

    @Test
    void testValidateSequenceNumberMoreThanMaxThrowsException() {
        final InterchangeHeader interchangeHeader =
            InterchangeHeader.fromString("UNB+UNOA:2+SNDR:80+RECP:80+190523:0900+100000000");

        final EdifactValidationException exception = assertThrows(EdifactValidationException.class,
            interchangeHeader::validate);

        assertEquals("UNB: Attribute sequenceNumber must be between 1 and 99999999", exception.getMessage());
    }

    @Test
    void testValidateSequenceNumberWithinMinMaxDoesNotThrowException() {
        final InterchangeHeader interchangeHeader =
            InterchangeHeader.fromString("UNB+UNOA:2+SNDR:80+RECP:80+190523:0900+00000001++MEDRPT");

        assertDoesNotThrow(interchangeHeader::validate);
    }

    @Test
    void testValidateMissingMessageTypeThrowsException() {
        final InterchangeHeader interchangeHeader =
            InterchangeHeader.fromString("UNB+UNOA:2+SNDR:80+RECP:80+190523:0900+00000001");

        assertThatThrownBy(interchangeHeader::validate)
            .isInstanceOf(EdifactValidationException.class)
            .hasMessage("UNB: Attribute messageType must be one of: [MEDRPT, NHSACK]");
    }

    @Test
    void testValidateInvalidMessageTypeThrowsException() {
        final InterchangeHeader interchangeHeader =
            InterchangeHeader.fromString("UNB+UNOA:2+SNDR:80+RECP:80+190523:0900+00000001++QWE");

        assertThatThrownBy(interchangeHeader::validate)
            .isInstanceOf(EdifactValidationException.class)
            .hasMessage("UNB: Attribute messageType must be one of: [MEDRPT, NHSACK]");
    }

    @Test
    void testValidationEmptySenderThrowsException() {
        final InterchangeHeader interchangeHeader =
            InterchangeHeader.fromString("UNB+UNOA:2++RECP:80+190523:0900+00000001");

        final EdifactValidationException exception = assertThrows(EdifactValidationException.class,
            interchangeHeader::validate);

        assertEquals("UNB: Attribute sender is required", exception.getMessage());
    }

    @Test
    void testValidationEmptyRecipientThrowsException() {
        final InterchangeHeader interchangeHeader =
            InterchangeHeader.fromString("UNB+UNOA:2+SNDR:80++190523:0900+00000001");

        final EdifactValidationException exception = assertThrows(EdifactValidationException.class,
            interchangeHeader::validate);

        assertEquals("UNB: Attribute recipient is required", exception.getMessage());
    }

    @Test
    void testFromStringWithValidEdifactStringReturnsInterchangeHeader() {
        final var interchangeHeader = InterchangeHeader.fromString("UNB+UNOA:2+SNDR:80+RECP:80+190323:0900+00000001");

        assertAll(
            () -> assertEquals(InterchangeHeader.KEY, interchangeHeader.getKey()),
            () -> assertEquals("SNDR", interchangeHeader.getSender()),
            () -> assertEquals("RECP", interchangeHeader.getRecipient()),
            () -> assertEquals(1L, interchangeHeader.getSequenceNumber()),
            () -> assertFalse(interchangeHeader.isNhsAckRequested())
        );
    }

    @Test
    void testFromStringWithInvalidEdifactStringThrowsException() {
        final IllegalArgumentException exception =
            assertThrows(IllegalArgumentException.class, () -> InterchangeHeader.fromString("wrong value"));

        assertEquals("Can't create InterchangeHeader from wrong value", exception.getMessage());
    }

    @Test
    void testFromStringWithNhsAckRequestedSetsTrue() {
        final var interchangeHeader = InterchangeHeader.fromString(
            "UNB+UNOA:2+SNDR:80+RECP:80+190323:0900+00000001++MEDRPT++1");

        assertAll(
            () -> assertEquals(InterchangeHeader.KEY, interchangeHeader.getKey()),
            () -> assertEquals("SNDR", interchangeHeader.getSender()),
            () -> assertEquals("RECP", interchangeHeader.getRecipient()),
            () -> assertEquals(1L, interchangeHeader.getSequenceNumber()),
            () -> assertTrue(interchangeHeader.isNhsAckRequested())
        );
    }

    @Test
    void testFromStringWithNoNhsAckRequestedSetsFalse() {
        final var interchangeHeader = InterchangeHeader.fromString(
            "UNB+UNOA:2+SNDR:80+RECP:80+190323:0900+00000001++MEDRPT++0");

        assertAll(
            () -> assertEquals(InterchangeHeader.KEY, interchangeHeader.getKey()),
            () -> assertEquals("SNDR", interchangeHeader.getSender()),
            () -> assertEquals("RECP", interchangeHeader.getRecipient()),
            () -> assertEquals(1L, interchangeHeader.getSequenceNumber()),
            () -> assertFalse(interchangeHeader.isNhsAckRequested())
        );
    }
}
