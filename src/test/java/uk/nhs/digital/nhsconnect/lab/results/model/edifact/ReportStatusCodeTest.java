package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;

public class ReportStatusCodeTest {

    @Test
    void testFromCodeForValidReportStatusCodeReturnsCode() {
        final ImmutableMap<String, ReportStatusCode> codeMap = ImmutableMap.<String, ReportStatusCode>builder()
            .put("UN", ReportStatusCode.UNSPECIFIED)
            .build();

        assertEquals(ReportStatusCode.values().length, codeMap.size());
        codeMap.forEach((code, statusCode) -> assertEquals(statusCode, ReportStatusCode.fromCode(code)));
    }

    @Test
    void testFromCodeForInvalidCodeThrowsException() {
        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> ReportStatusCode.fromCode("INVALID"));

        assertEquals("No Report Status Code for 'INVALID'", exception.getMessage());
    }

}
