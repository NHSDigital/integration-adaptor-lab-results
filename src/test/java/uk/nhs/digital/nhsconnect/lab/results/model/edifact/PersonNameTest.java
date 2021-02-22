package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import org.junit.jupiter.api.Test;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PersonNameTest {

    private static final String ID_AND_NAMES_VALUE = "PNA+PAT+RAT56:OPI+++SU:KENNEDY+FO:SARAH+TI:MISS+MI:ANGELA";
    private static final String NAMES_ONLY_VALUE = "PNA+PAT++++SU:KENNEDY+FO:SARAH+TI:MISS+MI:ANGELA";
    private static final String ID_ONLY = "PNA+PAT+RAT56:OPI";
    private static final String BLANK_ID_VALUE = "PNA+PAT+   +++SU:KENNEDY+FO:SARAH+TI:MISS+MI:ANGELA";

    @Test
    void testFromString() {
        var fromStringIdOnly = PersonName.fromString(ID_ONLY);
        var fromStringIdAndNames = PersonName.fromString(ID_AND_NAMES_VALUE);
        var fromStringNamesOnly = PersonName.fromString(NAMES_ONLY_VALUE);

        assertAll("fromStringIdOnly",
            () -> assertEquals(fromStringIdOnly.getNhsNumber(), "RAT56"),
            () -> assertNull(fromStringIdOnly.getFirstForename()),
            () -> assertNull(fromStringIdOnly.getSecondForename()),
            () -> assertNull(fromStringIdOnly.getSurname()),
            () -> assertNull(fromStringIdOnly.getTitle()));

        assertAll("fromStringIdAndNames",
            () -> assertEquals(fromStringIdAndNames.getNhsNumber(), "RAT56"),
            () -> assertEquals(fromStringIdAndNames.getFirstForename(), "SARAH"),
            () -> assertEquals(fromStringIdAndNames.getSecondForename(), "ANGELA"),
            () -> assertEquals(fromStringIdAndNames.getSurname(), "KENNEDY"),
            () -> assertEquals(fromStringIdAndNames.getTitle(), "MISS"));

        assertAll("fromStringNamesOnly",
            () -> assertNull(fromStringNamesOnly.getNhsNumber()),
            () -> assertEquals(fromStringNamesOnly.getFirstForename(), "SARAH"),
            () -> assertEquals(fromStringNamesOnly.getSecondForename(), "ANGELA"),
            () -> assertEquals(fromStringNamesOnly.getSurname(), "KENNEDY"),
            () -> assertEquals(fromStringNamesOnly.getTitle(), "MISS"));
    }

    @Test
    void testFromStringWithInvalidEdifactStringThrowsException() {
        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> PersonName.fromString("wrong value"));
        assertEquals("Can't create PersonName from wrong value", exception.getMessage());
    }

    @Test
    void testFromStringWithBlankIdentificationAndBlankNamesThrowsException() {
        final EdifactValidationException exception = assertThrows(EdifactValidationException.class,
            () -> PersonName.fromString("PNA+PAT+ +++ "));
        assertEquals("PNA: At least one of patient identification and person name details are required",
            exception.getMessage());
    }

    @Test
    void testFromStringWithBlankNhsNumberReturnsNull() {
        final String actual = PersonName.fromString(BLANK_ID_VALUE).getNhsNumber();
        assertNull(actual);
    }
}
