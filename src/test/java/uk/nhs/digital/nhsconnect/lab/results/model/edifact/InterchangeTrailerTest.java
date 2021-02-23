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
    void testValidateSequenceNumberNullThrowsException() {
        final InterchangeTrailer interchangeTrailer = InterchangeTrailer.fromString("UNZ+1");

        final EdifactValidationException exception = assertThrows(EdifactValidationException.class,
                interchangeTrailer::validate);

        assertEquals("UNZ: Attribute sequenceNumber is required", exception.getMessage());
    }

    @Test
    void testValidateSequenceNumberLessThanOrEqualToZeroThrowsException() {
        final InterchangeTrailer interchangeTrailer = InterchangeTrailer.fromString("UNZ+1+00000000");

        final EdifactValidationException exception = assertThrows(EdifactValidationException.class,
                interchangeTrailer::validate);

        assertEquals("UNZ: Attribute sequenceNumber is required", exception.getMessage());
    }

    @Test
    void testValidateSequenceNumberWithinMinMaxDoesNotThrowException() {
        final InterchangeTrailer interchangeTrailer = InterchangeTrailer.fromString("UNZ+1+00000001");

        assertDoesNotThrow(interchangeTrailer::validate);
    }

    @Test
    void testValidationNumberOfMessagesZero() {
        final InterchangeTrailer interchangeTrailer = InterchangeTrailer.fromString("UNZ+0+00000001");

        final EdifactValidationException exception = assertThrows(EdifactValidationException.class,
                interchangeTrailer::validate);

        assertEquals("UNZ: Attribute numberOfMessages is required", exception.getMessage());
    }

    @Test
    void testFromStringWithValidEdifactStringReturnsInterchangeTrailer() {
        final InterchangeTrailer interchangeTrailer = InterchangeTrailer.fromString("UNZ+18+00000003");

        assertEquals(InterchangeTrailer.KEY, interchangeTrailer.getKey());
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
