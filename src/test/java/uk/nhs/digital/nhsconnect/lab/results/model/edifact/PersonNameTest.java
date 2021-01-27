package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PersonNameTest {

    private static final String NHS_AND_NAMES = "PNA+PAT+RAT56:OPI+++SU:KENNEDY+FO:SARAH+TI:MISS+MI:ANGELA";
    private static final String NHS_AND_NAMES_VALUE = "PAT+RAT56:OPI+++SU:KENNEDY+FO:SARAH+TI:MISS+MI:ANGELA";
    private static final String NAMES_ONLY = "PNA+PAT++++SU:KENNEDY+FO:SARAH+TI:MISS+MI:ANGELA";
    private static final String NAMES_ONLY_VALUE = "PAT++++SU:KENNEDY+FO:SARAH+TI:MISS+MI:ANGELA";
    private static final String NHS_ONLY = "PNA+PAT+RAT56:OPI";
    private static final String NHS_ONLY_VALUE = "PAT+RAT56:OPI";
    private static final String BLANK_NHS_VALUE = "PNA+PAT+   +++SU:KENNEDY+FO:SARAH+TI:MISS+MI:ANGELA";


    @Test
    void testToEdifactReturnsForValidPersonName() {
        final String expected = "PNA+PAT+1234567890:OPI+++SU:STEVENS+FO:CHARLES+TI:MR+MI:ANTHONY'";

        final PersonName personName = PersonName.builder()
            .nhsNumber("1234567890")
            .patientIdentificationType(PatientIdentificationType.OFFICIAL_PATIENT_IDENTIFICATION)
            .surname("STEVENS")
            .firstForename("CHARLES")
            .title("MR")
            .secondForename("ANTHONY")
            .build();

        final String actual = personName.toEdifact();
        assertEquals(expected, actual);
    }

    @Test
    void testToEdifactWithTypeOnlyReturnsCorrectValue() {
        final String expected = "PNA+PAT+T247:OPI'";

        final PersonName personName = PersonName.builder()
            .nhsNumber("T247")
            .patientIdentificationType(PatientIdentificationType.OFFICIAL_PATIENT_IDENTIFICATION)
            .build();

        final String actual = personName.toEdifact();
        assertEquals(expected, actual);
    }

    @Test
    void testToEdifactWithEmptyNameReturnsEmptySegment() {
        final String expected = "PNA+PAT'";

        final PersonName personName = PersonName.builder().build();

        final String actual = personName.toEdifact();
        assertEquals(expected, actual);
    }

    @Test
    void testFromString() {
        assertEquals(NHS_ONLY_VALUE, PersonName.fromString(NHS_ONLY).getValue());
        assertEquals(NHS_AND_NAMES_VALUE, PersonName.fromString(NHS_AND_NAMES).getValue());
        assertEquals(NAMES_ONLY_VALUE, PersonName.fromString(NAMES_ONLY).getValue());
    }

    @Test
    void testFromStringWithInvalidEdifactStringThrowsException() {
        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> PersonName.fromString("wrong value"));
        assertEquals("Can't create PersonName from wrong value", exception.getMessage());
    }

    @Test
    void testFromStringWithBlankNhsNumberReturnsNull() {
        final String actual = PersonName.fromString(BLANK_NHS_VALUE).getNhsNumber();
        assertNull(actual);
    }
}
