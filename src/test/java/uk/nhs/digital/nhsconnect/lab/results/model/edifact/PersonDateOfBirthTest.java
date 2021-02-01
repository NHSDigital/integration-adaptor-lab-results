package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import org.junit.jupiter.api.Test;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PersonDateOfBirthTest {

    private static final String VALID_FHIR_DOB_CCYYMMDD = "1991-11-06";
    private static final String VALID_FHIR_DOB_CCYYMM = "1991-11";
    private static final String VALID_FHIR_DOB_CCYY = "1991";
    private static final String VALID_EDIFACT_CCYYMMDD = "DTM+329:19911106:102'";
    private static final String VALID_EDIFACT_CCYYMMDD_VALUE = "329:19911106:102";
    private static final String VALID_EDIFACT_CCYYMM = "DTM+329:199111:610'";
    private static final String VALID_EDIFACT_CCYYMM_VALUE = "329:199111:610";
    private static final String VALID_EDIFACT_CCYY = "DTM+329:1991:602'";
    private static final String VALID_EDIFACT_CCYY_VALUE = "329:1991:602";

    @Test
    void testToEdifactForValidPersonDateOfBirthInFormatCCYYMMDD() {
        final PersonDateOfBirth personDateOfBirth = PersonDateOfBirth.builder()
            .dateOfBirth(VALID_FHIR_DOB_CCYYMMDD)
            .dateFormat(DateFormat.CCYYMMDD)
            .build();

        final String actual = personDateOfBirth.toEdifact();

        assertEquals(VALID_EDIFACT_CCYYMMDD, actual);
    }

    @Test
    void testToEdifactForValidPersonDateOfBirthInFormatCCYYMM() {
        final PersonDateOfBirth personDateOfBirth = PersonDateOfBirth.builder()
            .dateOfBirth(VALID_FHIR_DOB_CCYYMM)
            .dateFormat(DateFormat.CCYYMM)
            .build();

        final String actual = personDateOfBirth.toEdifact();

        assertEquals(VALID_EDIFACT_CCYYMM, actual);
    }

    @Test
    void testToEdifactForValidPersonDateOfBirthInFormatCCYY() {
        final PersonDateOfBirth personDateOfBirth = PersonDateOfBirth.builder()
            .dateOfBirth(VALID_FHIR_DOB_CCYY)
            .dateFormat(DateFormat.CCYY)
            .build();

        final String actual = personDateOfBirth.toEdifact();

        assertEquals(VALID_EDIFACT_CCYY, actual);
    }

    @Test
    void testToEdifactForEmptyPersonDateOfBirthThrowsException() {
        final PersonDateOfBirth personDateOfBirth = PersonDateOfBirth.builder()
            .dateOfBirth("")
            .dateFormat(DateFormat.CCYYMMDD)
            .build();

        final EdifactValidationException exception = assertThrows(EdifactValidationException.class,
            personDateOfBirth::toEdifact);

        assertEquals("DTM: Date of birth is required", exception.getMessage());
    }

    @Test
    void testToEdifactForBlankPersonDateOfBirthThrowsException() {
        final PersonDateOfBirth personDateOfBirth = PersonDateOfBirth.builder()
            .dateOfBirth(" ")
            .dateFormat(DateFormat.CCYYMMDD)
            .build();

        final EdifactValidationException exception = assertThrows(EdifactValidationException.class,
            personDateOfBirth::toEdifact);

        assertEquals("DTM: Date of birth is required", exception.getMessage());
    }

    @Test
    void testFromStringWithValidEdifactStringReturnsPersonDateOfBirthInFormatCCYY() {
        final PersonDateOfBirth personDateOfBirth = PersonDateOfBirth.fromString(VALID_EDIFACT_CCYY);

        assertEquals(PersonDateOfBirth.KEY, personDateOfBirth.getKey());
        assertEquals(VALID_EDIFACT_CCYY_VALUE, personDateOfBirth.getValue());
        assertEquals(VALID_FHIR_DOB_CCYY, personDateOfBirth.getDateOfBirth());
    }

    @Test
    void testFromStringWithValidEdifactStringReturnsPersonDateOfBirthInFormatCCYYMM() {
        final PersonDateOfBirth personDateOfBirth = PersonDateOfBirth.fromString(VALID_EDIFACT_CCYYMM);

        assertEquals(PersonDateOfBirth.KEY, personDateOfBirth.getKey());
        assertEquals(VALID_EDIFACT_CCYYMM_VALUE, personDateOfBirth.getValue());
        assertEquals(VALID_FHIR_DOB_CCYYMM, personDateOfBirth.getDateOfBirth());
    }

    @Test
    void testFromStringWithValidEdifactStringReturnsPersonDateOfBirthInFormatCCYYMMDD() {
        final PersonDateOfBirth personDateOfBirth = PersonDateOfBirth.fromString(VALID_EDIFACT_CCYYMMDD);

        assertEquals(PersonDateOfBirth.KEY, personDateOfBirth.getKey());
        assertEquals(VALID_EDIFACT_CCYYMMDD_VALUE, personDateOfBirth.getValue());
        assertEquals(VALID_FHIR_DOB_CCYYMMDD, personDateOfBirth.getDateOfBirth());
    }

    @Test
    void testFromStringWithInvalidEdifactStringThrowsException() {
        final IllegalArgumentException exception =
            assertThrows(IllegalArgumentException.class, () -> PersonDateOfBirth.fromString("wrong value"));

        assertEquals("Can't create PersonDateOfBirth from wrong value", exception.getMessage());
    }

    @Test
    void testFromStringWithInvalidDateFormatCodeThrowsException() {
        final UnsupportedOperationException exception =
            assertThrows(UnsupportedOperationException.class, () -> PersonDateOfBirth.fromString("DTM+329:19911106:100'"));

        assertEquals("Date format code 100 is not supported", exception.getMessage());
    }
}
