package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PersonIdentificationTypeTest {

    @ParameterizedTest
    @EnumSource(PatientIdentificationType.class)
    void testFromCodeForValidCodeReturnsPatientIdentificationType(final PatientIdentificationType type) {
        assertEquals(type, PatientIdentificationType.fromCode(type.getCode()));
    }

    @Test
    void testFromCodeForInvalidCodeThrowsException() {
        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> PatientIdentificationType.fromCode("INVALID"));
        assertEquals("No patientIdentificationType name for 'INVALID'", exception.getMessage());
    }
}
