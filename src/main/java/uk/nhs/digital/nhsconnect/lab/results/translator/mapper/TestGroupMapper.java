package uk.nhs.digital.nhsconnect.lab.results.translator.mapper;

import org.hl7.fhir.dstu3.model.Observation;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Message;

import java.util.Optional;

public class TestGroupMapper {
    public Optional<Observation> map(final Message message) {
        return Optional.empty();
    }
}
