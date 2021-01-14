package uk.nhs.digital.nhsconnect.lab.results.inbound;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Interchange;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.InterchangeFactory;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.ToEdifactParsingException;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class EdifactParserTest {
    private static final String EDIFACT_HEADER = "UNB+UNOA:2+TES5+XX11+020114:1619+00000003";
    private static final String EDIFACT_TRAILER = "UNZ+1+00000003";

    private static final List<String> SAMPLE_EDIFACT = List.of(
            EDIFACT_HEADER + "'",
            "UNH+00000004+FHSREG:0:1:FH:FHS001'",
            "BGM+++507'",
            "NAD+FHS+XX1:954'",
            "DTM+137:199201141619:203'",
            "RFF+950:F4'",
            "RFF+TN:18'",
            "S01+1'",
            "NAD+GP+2750922,295:900'",
            "NAD+RIC+RT:956'",
            "QTY+951:6'",
            "QTY+952:3'",
            "HEA+ACD+A:ZZZ'",
            "HEA+ATP+2:ZZZ'",
            "HEA+BM+S:ZZZ'",
            "HEA+DM+Y:ZZZ'",
            "DTM+956:19920114:102'",
            "LOC+950+GLASGOW'",
            "FTX+RGI+++BABY AT THE REYNOLDS-THORPE CENTRE'",
            "S02+2'",
            "PNA+PAT+NHS123:OPI+++SU:KENNEDY+FO:SARAH+TI:MISS+MI:ANGELA'",
            "DTM+329:19911209:102'",
            "PDI+2'",
            "NAD+PAT++??:26 FARMSIDE CLOSE:ST PAULS CRAY:ORPINGTON:KENT+++++BR6  7ET'",
            "UNT+24+00000004'",
            EDIFACT_TRAILER + "'"
    );

    private static final List<String> TRAILER_BEFORE_HEADER_EDIFACT = List.of(
            EDIFACT_HEADER + "'",
            "UNT+24+00000004'", // message_trailer
            "BGM+++507'",
            "NAD+FHS+XX1:954'",
            "DTM+137:199201141619:203'",
            "RFF+950:F4'",
            "RFF+TN:18'",
            "S01+1'",
            "NAD+GP+2750922,295:900'",
            "NAD+RIC+RT:956'",
            "QTY+951:6'",
            "QTY+952:3'",
            "HEA+ACD+A:ZZZ'",
            "HEA+ATP+2:ZZZ'",
            "HEA+BM+S:ZZZ'",
            "HEA+DM+Y:ZZZ'",
            "DTM+956:19920114:102'",
            "LOC+950+GLASGOW'",
            "FTX+RGI+++BABY AT THE REYNOLDS-THORPE CENTRE'",
            "S02+2'",
            "PNA+PAT+NHS123:OPI+++SU:KENNEDY+FO:SARAH+TI:MISS+MI:ANGELA'",
            "DTM+329:19911209:102'",
            "PDI+2'",
            "NAD+PAT++??:26 FARMSIDE CLOSE:ST PAULS CRAY:ORPINGTON:KENT+++++BR6  7ET'",
            "UNH+00000004+FHSREG:0:1:FH:FHS001'", // message_header
            EDIFACT_TRAILER + "'"
    );


    private static final List<String> MISMATCH_MESSAGE_TRAILER_HEADER_EDIFACT = List.of(
            EDIFACT_HEADER + "'",
            "UNH+00000004+FHSREG:0:1:FH:FHS001'", // message_header
            "BGM+++507'",
            "NAD+FHS+XX1:954'",
            "DTM+137:199201141619:203'",
            "RFF+950:F4'",
            "RFF+TN:18'",
            "S01+1'",
            "NAD+GP+2750922,295:900'",
            "NAD+RIC+RT:956'",
            "UNT+24+00000004'", // message_trailer
            "QTY+951:6'",
            "QTY+952:3'",
            "HEA+ACD+A:ZZZ'",
            "HEA+ATP+2:ZZZ'",
            "HEA+BM+S:ZZZ'",
            "HEA+DM+Y:ZZZ'",
            "DTM+956:19920114:102'",
            "LOC+950+GLASGOW'",
            "FTX+RGI+++BABY AT THE REYNOLDS-THORPE CENTRE'",
            "S02+2'",
            "PNA+PAT+NHS123:OPI+++SU:KENNEDY+FO:SARAH+TI:MISS+MI:ANGELA'",
            "DTM+329:19911209:102'",
            "PDI+2'",
            "NAD+PAT++??:26 FARMSIDE CLOSE:ST PAULS CRAY:ORPINGTON:KENT+++++BR6  7ET'",
            "UNT+24+00000004'", // message_trailer
            EDIFACT_TRAILER + "'"
    );

    @Mock
    private InterchangeFactory interchangeFactory;

    @Mock
    private Interchange interchange;

    @InjectMocks
    private EdifactParser edifactParser;

    @Test
    void testParseCreatesInterchangeWithSameMessage() {
        when(interchangeFactory.createInterchange(any())).thenReturn(interchange);

        Interchange interchange = edifactParser.parse(String.join("\n", SAMPLE_EDIFACT));

        assertNotNull(interchange);

        // trailing empty string because we split by apostrophe and there's a trailing apostrophe
        verify(interchangeFactory).createInterchange(List.of(EDIFACT_HEADER, EDIFACT_TRAILER, ""));
    }

    @Test
    void testParsePropagatesExceptionWhenPassedTrailerBeforeHeader() {
        when(interchangeFactory.createInterchange(any())).thenReturn(interchange);

        ToEdifactParsingException toEdifactParsingException = assertThrows(ToEdifactParsingException.class,
                () -> edifactParser.parse(String.join("\n", TRAILER_BEFORE_HEADER_EDIFACT)));

        assertEquals("Message trailer before message header", toEdifactParsingException.getMessage());
    }

    @Test
    void testParsePropagatesExceptionWhenThereIsAMismatchOfHeadersAndTrailers() {
        when(interchangeFactory.createInterchange(any())).thenReturn(interchange);

        ToEdifactParsingException toEdifactParsingException = assertThrows(ToEdifactParsingException.class,
                () -> edifactParser.parse(String.join("\n", MISMATCH_MESSAGE_TRAILER_HEADER_EDIFACT)));


        assertEquals("Message header-trailer count mismatch: 1-2", toEdifactParsingException.getMessage());
    }

    @Test
    void testParsePropagatesExceptionsFromInvalidContent() {
        assertThrows(IndexOutOfBoundsException.class, () -> edifactParser.parse("invalid edifact"));
    }

}