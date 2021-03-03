package uk.nhs.digital.nhsconnect.lab.results.fixtures;

import java.util.List;

@SuppressWarnings("checkstyle:hideutilityclassconstructor")
public final class EdifactFixtures {
    public static final String EDIFACT_HEADER = "UNB+UNOA:2+000000004400001+000000024600002+020114:1619+00000003";
    public static final String EDIFACT_TRAILER = "UNZ+1+00000003";

    public static final List<String> SAMPLE_EDIFACT = List.of(
        EDIFACT_HEADER + "'",
        "UNH+00000004+FHSREG:0:1:FH:FHS001'",
        "BGM+++507'",
        "NAD+FHS+XX1:954'",
        "DTM+137:199201141619:203'",
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

    public static final List<String> TRAILER_BEFORE_HEADER_EDIFACT = List.of(
        EDIFACT_HEADER + "'",
        "UNT+24+00000004'", // message_trailer
        "BGM+++507'",
        "NAD+FHS+XX1:954'",
        "DTM+137:199201141619:203'",
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

    public static final List<String> MISMATCH_MESSAGE_TRAILER_HEADER_EDIFACT = List.of(
        EDIFACT_HEADER + "'",
        "UNH+00000004+FHSREG:0:1:FH:FHS001'", // message_header
        "BGM+++507'",
        "NAD+FHS+XX1:954'",
        "DTM+137:199201141619:203'",
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
}
