package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import org.junit.jupiter.api.Test;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PersonSexTest {

    private static final String VALID_EDIFACT = "PDI+1'";
    private static final String VALID_EDIFACT_VALUE = "1";

    @Test
    void testToEdifactForValidPersonGender() {
        final PersonSex personSex = PersonSex.builder()
            .gender(Gender.MALE)
            .build();

        final String actual = personSex.toEdifact();

        assertEquals(VALID_EDIFACT, actual);
    }

    @Test
    void testToEdifactForInvalidPersonGenderThrowsException() {
        final PersonSex personSex = PersonSex.builder().build();

        final EdifactValidationException exception = assertThrows(EdifactValidationException.class,
            personSex::toEdifact);

        assertEquals("PDI: Gender code is required", exception.getMessage());
    }

    @Test
    void testFromStringWithValidEdifactStringReturnsPersonGender() {
        final PersonSex personSex = PersonSex.fromString(VALID_EDIFACT);

        assertEquals(PersonSex.KEY, personSex.getKey());
        assertEquals(VALID_EDIFACT_VALUE, personSex.getValue());
        assertEquals(Gender.MALE, personSex.getGender());
    }

    @Test
    void testFromStringWithInvalidEdifactStringThrowsException() {
        final IllegalArgumentException exception =
            assertThrows(IllegalArgumentException.class, () -> PersonSex.fromString("wrong value"));

        assertEquals("Can't create PersonSex from wrong value", exception.getMessage());
    }
}
