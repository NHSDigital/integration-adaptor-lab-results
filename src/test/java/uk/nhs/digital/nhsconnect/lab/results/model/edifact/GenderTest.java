package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GenderTest {

    @Test
    void testFromCodeForValidCodeReturnsGender() {
        final ImmutableMap<String, Gender> codeMap = ImmutableMap.<String, Gender>builder()
            .put("0", Gender.UNKNOWN)
            .put("1", Gender.MALE)
            .put("2", Gender.FEMALE)
            .put("9", Gender.OTHER)
            .build();

        assertEquals(Gender.values().length, codeMap.size());
        codeMap.forEach((code, gender) -> assertEquals(gender, Gender.fromCode(code)));
    }

    @Test
    void testFromCodeForInvalidCodeThrowsException() {
        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> Gender.fromCode("INVALID"));
        assertEquals("No gender name for 'INVALID'", exception.getMessage());
    }
}
