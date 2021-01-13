package uk.nhs.digital.nhsconnect.lab.results.inbound;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Interchange;

public class EdifactParserTest {

    private final String edifactString = "UNB+UNOA:2+TES5+XX11+020114:1619+00000003'\n" +
            "UNH+00000004+FHSREG:0:1:FH:FHS001'\n" +
            "BGM+++507'\n" +
            "NAD+FHS+XX1:954'\n" +
            "DTM+137:199201141619:203'\n" +
            "RFF+950:F4'\n" +
            "RFF+TN:18'\n" +
            "S01+1'\n" +
            "NAD+GP+2750922,295:900'\n" +
            "NAD+RIC+RT:956'\n" +
            "QTY+951:6'\n" +
            "QTY+952:3'\n" +
            "HEA+ACD+A:ZZZ'\n" +
            "HEA+ATP+2:ZZZ'\n" +
            "HEA+BM+S:ZZZ'\n" +
            "HEA+DM+Y:ZZZ'\n" +
            "DTM+956:19920114:102'\n" +
            "LOC+950+GLASGOW'\n" +
            "FTX+RGI+++BABY AT THE REYNOLDS-THORPE CENTRE'\n" +
            "S02+2'\n" +
            "PNA+PAT+NHS123:OPI+++SU:KENNEDY+FO:SARAH+TI:MISS+MI:ANGELA'\n" +
            "DTM+329:19911209:102'\n" +
            "PDI+2'\n" +
            "NAD+PAT++??:26 FARMSIDE CLOSE:ST PAULS CRAY:ORPINGTON:KENT+++++BR6  7ET'\n" +
            "UNT+24+00000004'\n" +
            "UNZ+1+00000003'";

    @Test
    void testInterchangeHeader() {
        Interchange interchange = new EdifactParser().parse(edifactString);
        assertEquals("UNOA:2+TES5+XX11+020114:1619+00000003", interchange.getInterchangeHeader().getValue());
    }

    @Test
    void testInterchangeTrailer() {
        Interchange interchange = new EdifactParser().parse(edifactString);
        assertEquals("1+00000003", interchange.getInterchangeTrailer().getValue());
    }

}
