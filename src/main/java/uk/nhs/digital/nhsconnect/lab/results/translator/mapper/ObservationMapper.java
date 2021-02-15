package uk.nhs.digital.nhsconnect.lab.results.translator.mapper;

import org.hl7.fhir.dstu3.model.Observation;
import org.springframework.stereotype.Component;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Message;

import java.util.List;

@Component
public class ObservationMapper {
    public List<Observation> mapToTestGroups(final Message message) {
        return List.of(new Observation());
    }

    public List<Observation> mapToTestResults(final Message message) {
        return List.of(new Observation());
    }
}
