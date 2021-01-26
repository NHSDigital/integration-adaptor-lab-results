package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import org.junit.jupiter.api.Test;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InterchangeHeaderTest {

    private static final long LARGE_SEQUENCE_NUMBER = 100_000_000L;
    @SuppressWarnings("checkstyle:magicnumber")
    private final Instant translationWinterDateTime = ZonedDateTime
        .of(2019, 3, 23, 9, 0, 0, 0, ZoneOffset.UTC)
        .toInstant();
    @SuppressWarnings("checkstyle:magicnumber")
    private final Instant translationSummerDateTime = ZonedDateTime
        .of(2019, 5, 23, 9, 0, 0, 0, ZoneOffset.UTC)
        .toInstant();
    private final InterchangeHeader interchangeHeaderWinter = new InterchangeHeader("SNDR", "RECP", translationWinterDateTime)
        .setSequenceNumber(1L);
    private final InterchangeHeader interchangeHeaderSummer = new InterchangeHeader("SNDR", "RECP", translationSummerDateTime)
        .setSequenceNumber(1L);

    @Test
    void testToEdifactForValidInterchangeHeaderWithWinterTime() {
        final String edifact = interchangeHeaderWinter.toEdifact();

        assertEquals("UNB+UNOA:2+SNDR+RECP+190323:0900+00000001'", edifact);
    }

    @Test
    void testToEdifactForValidInterchangeHeaderWithSummerTime() {
        final String edifact = interchangeHeaderSummer.toEdifact();

        assertEquals("UNB+UNOA:2+SNDR+RECP+190523:1000+00000001'", edifact);
    }

    @Test
    void testGetValueForValidInterchangeHeader() {
        final InterchangeHeader interchangeHeader =
                new InterchangeHeader("SNDR", "RECP", translationSummerDateTime);
        interchangeHeader.setSequenceNumber(1L);

        final String interchangeHeaderValue = interchangeHeader.getValue();

        assertEquals("UNOA:2+SNDR+RECP+190523:1000+00000001", interchangeHeaderValue);
    }

    @Test
    void testValidateStatefulSequenceNumberNullThrowsException() {
        final InterchangeHeader interchangeHeader =
                new InterchangeHeader("SNDR", "RECP", translationSummerDateTime);

        final EdifactValidationException exception = assertThrows(EdifactValidationException.class,
                interchangeHeader::validateStateful);

        assertEquals("UNB: Attribute sequenceNumber is required", exception.getMessage());
    }

    @Test
    void testValidateStatefulSequenceNumberLessThanOneThrowsException() {
        final InterchangeHeader interchangeHeader =
                new InterchangeHeader("SNDR", "RECP", translationSummerDateTime);
        interchangeHeader.setSequenceNumber(0L);

        final EdifactValidationException exception = assertThrows(EdifactValidationException.class,
                interchangeHeader::validateStateful);

        assertEquals("UNB: Attribute sequenceNumber must be between 1 and 99999999", exception.getMessage());
    }

    @Test
    void testValidateStatefulSequenceNumberMoreThanMaxThrowsException() {
        final InterchangeHeader interchangeHeader =
                new InterchangeHeader("SNDR", "RECP", translationSummerDateTime);
        interchangeHeader.setSequenceNumber(LARGE_SEQUENCE_NUMBER);

        final EdifactValidationException exception = assertThrows(EdifactValidationException.class,
                interchangeHeader::validateStateful);

        assertEquals("UNB: Attribute sequenceNumber must be between 1 and 99999999", exception.getMessage());
    }

    @Test
    void testValidateStatefulSequenceNumberWithinMinMaxDoesNotThrowException() {
        final InterchangeHeader interchangeHeader =
                new InterchangeHeader("SNDR", "RECP", translationSummerDateTime);
        interchangeHeader.setSequenceNumber(1L);

        assertDoesNotThrow(interchangeHeader::validateStateful);
    }

    @Test
    void testPreValidationEmptySenderThrowsException() {
        final InterchangeHeader interchangeHeader = new InterchangeHeader("", "RECP", translationSummerDateTime);
        interchangeHeader.setSequenceNumber(1L);

        final EdifactValidationException exception = assertThrows(EdifactValidationException.class,
                interchangeHeader::preValidate);

        assertEquals("UNB: Attribute sender is required", exception.getMessage());
    }

    @Test
    void testPreValidationEmptyRecipientThrowsException() {
        final InterchangeHeader interchangeHeader = new InterchangeHeader("SNDR", "", translationSummerDateTime);
        interchangeHeader.setSequenceNumber(1L);

        final EdifactValidationException exception = assertThrows(EdifactValidationException.class,
                interchangeHeader::preValidate);

        assertEquals("UNB: Attribute recipient is required", exception.getMessage());
    }

    @Test
    void testFromStringWithValidEdifactStringReturnsInterchangeHeader() {
        final InterchangeHeader interchangeHeader = InterchangeHeader.fromString("UNB+UNOA:2+SNDR+RECP+190323:0900+00000001");

        assertEquals(InterchangeHeader.KEY, interchangeHeader.getKey());
        assertEquals("UNOA:2+SNDR+RECP+190323:0900+00000001", interchangeHeader.getValue());
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
