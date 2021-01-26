package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import org.junit.jupiter.api.Test;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InterchangeTrailerTest {

    private static final int NUMBER_OF_MESSAGES = 18;
    private static final long SEQUENCE_NUMBER = 3L;

    @Test
    void testToEdifactForValidInterchangeTrailer() {
        final InterchangeTrailer interchangeTrailer = new InterchangeTrailer(1);
        interchangeTrailer.setSequenceNumber(1L);

        final String edifact = interchangeTrailer.toEdifact();

        assertEquals("UNZ+1+00000001'", edifact);
    }

    @Test
    void testToEdifactForMissingSequenceNumberThrowsException() {
        final InterchangeTrailer interchangeTrailer = new InterchangeTrailer(1);

        final EdifactValidationException exception = assertThrows(EdifactValidationException.class,
                interchangeTrailer::toEdifact);

        assertEquals("UNZ: Attribute sequenceNumber is required", exception.getMessage());
    }

    @Test
    void testToEdifactForZeroNumberOfMessagesThrowsException() {
        final InterchangeTrailer interchangeTrailer = new InterchangeTrailer(0);
        interchangeTrailer.setSequenceNumber(1L);

        final EdifactValidationException exception = assertThrows(EdifactValidationException.class,
                interchangeTrailer::toEdifact);

        assertEquals("UNZ: Attribute numberOfMessages is required", exception.getMessage());
    }

    @Test
    void testGetValueForValidInterchangeTrailer() {
        final InterchangeTrailer interchangeTrailer = new InterchangeTrailer(1);
        interchangeTrailer.setSequenceNumber(1L);

        final String interchangeTrailerValue = interchangeTrailer.getValue();

        assertEquals("1+00000001", interchangeTrailerValue);
    }

    @Test
    void testValidateStatefulSequenceNumberNullThrowsException() {
        final InterchangeTrailer interchangeTrailer = new InterchangeTrailer(1);

        final EdifactValidationException exception = assertThrows(EdifactValidationException.class,
                interchangeTrailer::validateStateful);

        assertEquals("UNZ: Attribute sequenceNumber is required", exception.getMessage());
    }

    @Test
    void testValidateStatefulSequenceNumberLessThanOrEqualToZeroThrowsException() {
        final InterchangeTrailer interchangeTrailer = new InterchangeTrailer(1);
        interchangeTrailer.setSequenceNumber(0L);

        final EdifactValidationException exception = assertThrows(EdifactValidationException.class,
                interchangeTrailer::validateStateful);

        assertEquals("UNZ: Attribute sequenceNumber is required", exception.getMessage());
    }

    @Test
    void testValidateStatefulSequenceNumberWithinMinMaxDoesNotThrowException() {
        final InterchangeTrailer interchangeTrailer = new InterchangeTrailer(1);
        interchangeTrailer.setSequenceNumber(1L);

        assertDoesNotThrow(interchangeTrailer::validateStateful);
    }

    @Test
    void testPreValidationNumberOfMessagesZero() {
        final InterchangeTrailer interchangeTrailer = new InterchangeTrailer(0);
        interchangeTrailer.setSequenceNumber(1L);

        final EdifactValidationException exception = assertThrows(EdifactValidationException.class,
                interchangeTrailer::preValidate);

        assertEquals("UNZ: Attribute numberOfMessages is required", exception.getMessage());
    }

    @Test
    void testFromStringWithValidEdifactStringReturnsInterchangeTrailer() {
        final InterchangeTrailer interchangeTrailer = InterchangeTrailer.fromString("UNZ+18+00000003'");

        assertEquals(InterchangeTrailer.KEY, interchangeTrailer.getKey());
        assertEquals("18+00000003", interchangeTrailer.getValue());
        assertEquals(NUMBER_OF_MESSAGES, interchangeTrailer.getNumberOfMessages());
        assertEquals(SEQUENCE_NUMBER, interchangeTrailer.getSequenceNumber());
    }

    @Test
    void testFromStringWithInvalidEdifactStringThrowsException() {
        final IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> InterchangeTrailer.fromString("wrong value"));

        assertEquals("Can't create InterchangeTrailer from wrong value", exception.getMessage());
    }
}
