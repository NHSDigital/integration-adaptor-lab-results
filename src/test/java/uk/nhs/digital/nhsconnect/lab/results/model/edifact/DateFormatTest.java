package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DateFormatTest {

    @ParameterizedTest
    @EnumSource(DateFormat.class)
    void testFromCodeForValidCodeReturnsDateFormat(final DateFormat dateFormat) {
        assertEquals(dateFormat, DateFormat.fromCode(dateFormat.getCode()));
    }

    @Test
    void testFromCodeForInvalidCodeThrowsException() {
        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> DateFormat.fromCode("INVALID"));
        assertEquals("No dateFormat name for 'INVALID'", exception.getMessage());
    }
}
