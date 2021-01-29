package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DateFormatTest {

    @Test
    void testFromCodeForValidCodeReturnsDateFormat() {
        final ImmutableMap<String, DateFormat> codeMap = ImmutableMap.<String, DateFormat>builder()
            .put("102", DateFormat.CCYYMMDD)
            .put("602", DateFormat.CCYY)
            .put("610", DateFormat.CCYYMM)
            .build();

        assertEquals(DateFormat.values().length, codeMap.size());
        codeMap.forEach((code, dateFormat) -> assertEquals(dateFormat, DateFormat.fromCode(code)));
    }

    @Test
    void testFromCodeForInvalidCodeThrowsException() {
        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> DateFormat.fromCode("INVALID"));
        assertEquals("No dateFormat name for 'INVALID'", exception.getMessage());
    }
}
