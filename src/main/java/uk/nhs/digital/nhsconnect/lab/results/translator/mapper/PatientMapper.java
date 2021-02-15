package uk.nhs.digital.nhsconnect.lab.results.translator.mapper;

import org.hl7.fhir.dstu3.model.Patient;
import org.springframework.stereotype.Component;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Message;

@Component
public class PatientMapper {
    public Patient mapToPatient(final Message message) {
        return new Patient();
    }
}
