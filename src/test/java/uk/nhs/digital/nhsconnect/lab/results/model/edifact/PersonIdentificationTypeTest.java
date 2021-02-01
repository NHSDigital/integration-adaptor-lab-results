package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PersonIdentificationTypeTest {

    @Test
    void testFromCodeForValidCodeReturnsPatientIdentificationType() {
        final ImmutableMap<String, PatientIdentificationType> codeMap =
            ImmutableMap.of("OPI", PatientIdentificationType.OFFICIAL_PATIENT_IDENTIFICATION);

        assertEquals(PatientIdentificationType.values().length, codeMap.size());

        codeMap.forEach((code, patientIdentificationType)
            -> assertEquals(patientIdentificationType, PatientIdentificationType.fromCode(code)));
    }

    @Test
    void testFromCodeForInvalidCodeThrowsException() {
        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> PatientIdentificationType.fromCode("INVALID"));
        assertEquals("No patientIdentificationType name for 'INVALID'", exception.getMessage());
    }
}
