package uk.nhs.digital.nhsconnect.lab.results.inbound;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.digital.nhsconnect.lab.results.fixtures.EdifactFixtures;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Interchange;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.InterchangeHeader;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.InterchangeTrailer;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.InterchangeCriticalException;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.InterchangeFactory;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.InterchangeParsingException;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.MessageParsingException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EdifactParserTest {
    private static final String SENDER = "some_sender";
    private static final String RECIPIENT = "some_recipient";
    private static final Long INTERCHANGE_SEQUENCE_NUMBER = 1L;

    @Mock
    private InterchangeFactory interchangeFactory;

    @Mock
    private Interchange interchange;

    @Mock
    private InterchangeHeader interchangeHeader;

    @Mock
    private InterchangeTrailer interchangeTrailer;

    @InjectMocks
    private EdifactParser edifactParser;

    @Test
    void testParseCreatesInterchangeWithSameMessage() throws InterchangeParsingException, MessageParsingException {
        when(interchangeTrailer.getNumberOfMessages()).thenReturn(1);
        when(interchange.getInterchangeTrailer()).thenReturn(interchangeTrailer);

        when(interchangeFactory.createInterchange(any())).thenReturn(interchange);
        when(interchange.setMessages(any())).thenReturn(interchange);

        Interchange interchange = edifactParser.parse(EdifactFixtures.SAMPLE_EDIFACT);

        assertNotNull(interchange);

        // trailing empty string because we split by apostrophe and there's a trailing apostrophe
        verify(interchangeFactory).createInterchange(
            List.of(EdifactFixtures.EDIFACT_INTERCHANGE_HEADER, EdifactFixtures.EDIFACT_INTERCHANGE_TRAILER, "")
        );
    }

    @Test
    void testParsePropagatesExceptionWhenPassedTrailerBeforeHeader() {
        when(interchangeHeader.getSender()).thenReturn(SENDER);
        when(interchangeHeader.getRecipient()).thenReturn(RECIPIENT);
        when(interchangeHeader.getSequenceNumber()).thenReturn(INTERCHANGE_SEQUENCE_NUMBER);

        when(interchange.getInterchangeHeader()).thenReturn(interchangeHeader);

        when(interchangeFactory.createInterchange(any())).thenReturn(interchange);

        assertThatThrownBy(() ->
            edifactParser.parse(EdifactFixtures.TRAILER_BEFORE_HEADER_EDIFACT))
            .isInstanceOf(MessageParsingException.class)
            .hasMessage("Error parsing messages");
    }

    @Test
    void testParsePropagatesExceptionWhenThereIsAMismatchOfHeadersAndTrailers() {
        when(interchangeHeader.getSender()).thenReturn(SENDER);
        when(interchangeHeader.getRecipient()).thenReturn(RECIPIENT);
        when(interchangeHeader.getSequenceNumber()).thenReturn(INTERCHANGE_SEQUENCE_NUMBER);

        when(interchange.getInterchangeHeader()).thenReturn(interchangeHeader);

        when(interchangeFactory.createInterchange(any())).thenReturn(interchange);

        assertThatThrownBy(() -> edifactParser.parse(EdifactFixtures.MISMATCH_MESSAGE_TRAILER_HEADER_EDIFACT))
            .isInstanceOf(MessageParsingException.class)
            .hasMessage("Error parsing messages");
    }

    @Test
    void testParsePropagatesExceptionsFromInvalidContent() {
        assertThrows(InterchangeCriticalException.class, () -> edifactParser.parse("invalid edifact"));
    }
}
