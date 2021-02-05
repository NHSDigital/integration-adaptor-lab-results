package uk.nhs.digital.nhsconnect.lab.results.translator.mapper;

import org.hl7.fhir.dstu3.model.Organization;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Message;

import java.util.Optional;

public class RequestingOrgMapper {
    public Optional<Organization> map(final Message message) {
        return Optional.empty();
    }
}
