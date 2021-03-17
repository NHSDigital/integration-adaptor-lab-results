package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import uk.nhs.digital.nhsconnect.lab.results.model.enums.ReportStatusCode;

public class ReportStatusCodeTest {

    @ParameterizedTest
    @EnumSource(ReportStatusCode.class)
    void testFromCodeForValidReportStatusCodeReturnsCode(final ReportStatusCode reportStatusCode) {
        assertEquals(reportStatusCode, ReportStatusCode.fromCode(reportStatusCode.getCode()));
    }

    @Test
    void testFromCodeForInvalidCodeThrowsException() {
        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> ReportStatusCode.fromCode("INVALID"));

        assertEquals("No Report Status Code for 'INVALID'", exception.getMessage());
    }

}
