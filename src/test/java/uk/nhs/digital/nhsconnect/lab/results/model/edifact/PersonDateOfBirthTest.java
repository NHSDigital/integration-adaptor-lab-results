package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PersonDateOfBirthTest {

    private static final String VALID_FHIR_DOB_CCYYMMDD = "1991-11-06";
    private static final String VALID_FHIR_DOB_CCYYMM = "1991-11";
    private static final String VALID_FHIR_DOB_CCYY = "1991";
    private static final String VALID_EDIFACT_CCYYMMDD = "DTM+329:19911106:102'";
    private static final String VALID_EDIFACT_CCYYMM = "DTM+329:199111:610'";
    private static final String VALID_EDIFACT_CCYY = "DTM+329:1991:602'";

    @Test
    void testFromStringWithValidEdifactStringReturnsPersonDateOfBirthInFormatCCYY() {
        final PersonDateOfBirth personDateOfBirth = PersonDateOfBirth.fromString(VALID_EDIFACT_CCYY);

        assertAll("fromString date of birth format CCYY",
            () -> assertEquals(PersonDateOfBirth.KEY, personDateOfBirth.getKey()),
            () -> assertEquals(VALID_FHIR_DOB_CCYY, personDateOfBirth.getDateOfBirth()));
    }

    @Test
    void testFromStringWithValidEdifactStringReturnsPersonDateOfBirthInFormatCCYYMM() {
        final PersonDateOfBirth personDateOfBirth = PersonDateOfBirth.fromString(VALID_EDIFACT_CCYYMM);

        assertAll("fromString date of birth format CCYYMM",
            () -> assertEquals(PersonDateOfBirth.KEY, personDateOfBirth.getKey()),
            () -> assertEquals(VALID_FHIR_DOB_CCYYMM, personDateOfBirth.getDateOfBirth()));
    }

    @Test
    void testFromStringWithValidEdifactStringReturnsPersonDateOfBirthInFormatCCYYMMDD() {
        final PersonDateOfBirth personDateOfBirth = PersonDateOfBirth.fromString(VALID_EDIFACT_CCYYMMDD);

        assertAll("fromString date of birth format CCYYMMDD",
            () -> assertEquals(PersonDateOfBirth.KEY, personDateOfBirth.getKey()),
            () -> assertEquals(VALID_FHIR_DOB_CCYYMMDD, personDateOfBirth.getDateOfBirth()));
    }

    @Test
    void testFromStringWithInvalidEdifactStringThrowsException() {
        final IllegalArgumentException exception =
            assertThrows(IllegalArgumentException.class, () -> PersonDateOfBirth.fromString("wrong value"));

        assertEquals("Can't create PersonDateOfBirth from wrong value", exception.getMessage());
    }

    @Test
    void testFromStringWithInvalidDateFormatCodeThrowsException() {
        final UnsupportedOperationException exception = assertThrows(UnsupportedOperationException.class,
            () -> PersonDateOfBirth.fromString("DTM+329:19911106:100'"));

        assertEquals("DTM: Date format code 100 is not supported", exception.getMessage());
    }
}
