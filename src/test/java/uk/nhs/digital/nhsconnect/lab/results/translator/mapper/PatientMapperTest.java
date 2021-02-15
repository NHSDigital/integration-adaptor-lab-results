package uk.nhs.digital.nhsconnect.lab.results.translator.mapper;

import org.hl7.fhir.dstu3.model.Patient;
import org.junit.jupiter.api.Test;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Message;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

class PatientMapperTest {

    @Test
    void testMapMessageToPatient() {
        final Message message = new Message(new ArrayList<>());

        assertThat(new PatientMapper().mapToPatient(message)).isExactlyInstanceOf(Patient.class);
    }
}
