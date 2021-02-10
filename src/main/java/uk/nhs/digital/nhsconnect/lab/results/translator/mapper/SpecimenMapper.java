package uk.nhs.digital.nhsconnect.lab.results.translator.mapper;

import org.hl7.fhir.dstu3.model.Specimen;
import org.springframework.stereotype.Component;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Message;

import java.util.List;

@Component
public class SpecimenMapper {
    public List<Specimen> mapToSpecimens(final Message message) {
        return List.of(new Specimen());
    }
}
