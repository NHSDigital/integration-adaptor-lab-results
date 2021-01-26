package uk.nhs.digital.nhsconnect.lab.results.inbound;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.digital.nhsconnect.lab.results.fixtures.EdifactFixtures;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Interchange;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.InterchangeFactory;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.ToEdifactParsingException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EdifactParserTest {
    @Mock
    private InterchangeFactory interchangeFactory;

    @Mock
    private Interchange interchange;

    @InjectMocks
    private EdifactParser edifactParser;

    @Test
    void testParseCreatesInterchangeWithSameMessage() {
        when(interchangeFactory.createInterchange(any())).thenReturn(interchange);

        Interchange interchange = edifactParser.parse(String.join("\n", EdifactFixtures.SAMPLE_EDIFACT));

        assertNotNull(interchange);

        // trailing empty string because we split by apostrophe and there's a trailing apostrophe
        verify(interchangeFactory).createInterchange(
            List.of(EdifactFixtures.EDIFACT_HEADER, EdifactFixtures.EDIFACT_TRAILER, ""));
    }

    @Test
    void testParsePropagatesExceptionWhenPassedTrailerBeforeHeader() {
        when(interchangeFactory.createInterchange(any())).thenReturn(interchange);

        ToEdifactParsingException toEdifactParsingException = assertThrows(ToEdifactParsingException.class,
            () -> edifactParser.parse(String.join("\n", EdifactFixtures.TRAILER_BEFORE_HEADER_EDIFACT)));

        assertEquals("Message trailer before message header", toEdifactParsingException.getMessage());
    }

    @Test
    void testParsePropagatesExceptionWhenThereIsAMismatchOfHeadersAndTrailers() {
        when(interchangeFactory.createInterchange(any())).thenReturn(interchange);

        ToEdifactParsingException toEdifactParsingException = assertThrows(ToEdifactParsingException.class,
            () -> edifactParser.parse(String.join("\n", EdifactFixtures.MISMATCH_MESSAGE_TRAILER_HEADER_EDIFACT)));


        assertEquals("Message header-trailer count mismatch: 1-2", toEdifactParsingException.getMessage());
    }

    @Test
    void testParsePropagatesExceptionsFromInvalidContent() {
        assertThrows(IndexOutOfBoundsException.class, () -> edifactParser.parse("invalid edifact"));
    }
}
