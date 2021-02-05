package uk.nhs.digital.nhsconnect.lab.results.translator.mapper;

import org.hl7.fhir.dstu3.model.Patient;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Message;

public class PatientMapper {
    public Patient map(final Message message) {
        return new Patient();
    }
}
