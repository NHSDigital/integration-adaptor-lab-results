package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import org.junit.jupiter.api.Test;
import uk.nhs.digital.nhsconnect.lab.results.model.enums.Gender;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PersonSexTest {

    private static final String VALID_EDIFACT = "PDI+1";

    @Test
    void testFromStringWithValidEdifactStringReturnsPersonGender() {
        final PersonSex personSex = PersonSex.fromString(VALID_EDIFACT);

        assertAll("fromString",
            () -> assertEquals(PersonSex.KEY, personSex.getKey()),
            () -> assertEquals(Gender.MALE, personSex.getGender()));
    }

    @Test
    void testFromStringWithInvalidEdifactStringThrowsException() {
        final IllegalArgumentException exception =
            assertThrows(IllegalArgumentException.class, () -> PersonSex.fromString("wrong value"));

        assertEquals("Can't create PersonSex from wrong value", exception.getMessage());
    }

    @Test
    void testBuilderWithNullGenderThrowsNullPointerException() {
        assertThrows(NullPointerException.class, () -> PersonSex.builder().build());
    }
}
