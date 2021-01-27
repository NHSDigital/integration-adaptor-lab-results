package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import org.junit.jupiter.api.Test;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PersonDateOfBirthTest {

    private static final LocalDate FIXED_TIME = LocalDate.of(1991, 11, 6);
    private static final String VALID_EDIFACT = "DTM+329:19911106:102'";
    private static final String VALID_EDIFACT_VALUE = "329:19911106:102";

    @Test
    void testToEdifactForValidPersonDateOfBirth() {
        final PersonDateOfBirth personDateOfBirth = PersonDateOfBirth.builder()
            .dateOfBirth(FIXED_TIME)
            .build();

        final String actual = personDateOfBirth.toEdifact();

        assertEquals(VALID_EDIFACT, actual);
    }

    @Test
    void testToEdifactForInvalidPersonDateOfBirthThrowsException() {
        final PersonDateOfBirth personDateOfBirth = PersonDateOfBirth.builder().build();

        final EdifactValidationException exception = assertThrows(EdifactValidationException.class,
            personDateOfBirth::toEdifact);

        assertEquals("DTM: Date of birth is required", exception.getMessage());
    }

    @Test
    void testFromStringWithValidEdifactStringReturnsPersonDateOfBirth() {
        final PersonDateOfBirth personDateOfBirth = PersonDateOfBirth.fromString(VALID_EDIFACT);

        assertEquals("DTM", personDateOfBirth.getKey());
        assertEquals(VALID_EDIFACT_VALUE, personDateOfBirth.getValue());
        assertEquals(FIXED_TIME, personDateOfBirth.getDateOfBirth());
    }

    @Test
    void testFromStringWithInvalidEdifactStringThrowsException() {
        final IllegalArgumentException exception =
            assertThrows(IllegalArgumentException.class, () -> PersonDateOfBirth.fromString("wrong value"));

        assertEquals("Can't create PersonDateOfBirth from wrong value", exception.getMessage());
    }
}
