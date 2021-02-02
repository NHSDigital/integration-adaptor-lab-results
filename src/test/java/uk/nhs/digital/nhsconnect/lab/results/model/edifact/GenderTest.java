package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GenderTest {

    @ParameterizedTest
    @EnumSource(Gender.class)
    void testFromCodeForValidCodeReturnsGender(final Gender gender) {
        assertEquals(gender, Gender.fromCode(gender.getCode()));
    }

    @Test
    void testFromCodeForInvalidCodeThrowsException() {
        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> Gender.fromCode("INVALID"));
        assertEquals("No gender name for 'INVALID'", exception.getMessage());
    }
}
