package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import org.junit.jupiter.api.Test;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class InterchangeTrailerTest {

    @Test
    public void testValidInterchangeTrailer() throws EdifactValidationException {
        InterchangeTrailer interchangeTrailer = new InterchangeTrailer(1);
        interchangeTrailer.setSequenceNumber(1L);

        String edifact = interchangeTrailer.toEdifact();

        assertEquals("UNZ+1+00000001'", edifact);
    }

    @Test
    public void testPreValidationNumberOfMessagesZero() {
        InterchangeTrailer interchangeTrailer = new InterchangeTrailer(0);
        interchangeTrailer.setSequenceNumber(1L);

        Exception exception = assertThrows(EdifactValidationException.class, interchangeTrailer::preValidate);

        String expectedMessage = "UNZ: Attribute numberOfMessages is required";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void testPreValidationSequenceNumberMissing() {
        InterchangeTrailer interchangeTrailer = new InterchangeTrailer(1);

        Exception exception = assertThrows(EdifactValidationException.class, interchangeTrailer::toEdifact);

        String expectedMessage = "UNZ: Attribute sequenceNumber is required";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void testFromString() {
        final var numberOfMessages = 18;
        final var sequenceNumber = 3L;
        var expectedInterchangeTrailer = new InterchangeTrailer(numberOfMessages)
                .setSequenceNumber(sequenceNumber);
        var edifact = "UNZ+" + numberOfMessages + "+0000000" + sequenceNumber + "'";

        var interchangeTrailer = InterchangeTrailer.fromString(edifact);

        assertThat(interchangeTrailer).isExactlyInstanceOf(InterchangeTrailer.class);
        assertThat(interchangeTrailer).isEqualToComparingFieldByField(expectedInterchangeTrailer);
        assertThat(interchangeTrailer.toEdifact()).isEqualTo(edifact);
    }
}
