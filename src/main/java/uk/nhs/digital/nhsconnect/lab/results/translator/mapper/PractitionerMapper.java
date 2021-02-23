package uk.nhs.digital.nhsconnect.lab.results.translator.mapper;

import org.hl7.fhir.dstu3.model.Practitioner;
import org.springframework.stereotype.Component;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Message;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.RequesterNameAndAddress;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.segmentgroup.InvolvedParty;

import java.util.Optional;

@Component
public class PractitionerMapper {
    private static final String SDS_USER_SYSTEM = "https://fhir.nhs.uk/Id/sds-user-id";

    public Optional<Practitioner> mapRequester(final Message message) {
        return message.getInvolvedParties().stream()
            .map(InvolvedParty::getRequesterNameAndAddress)
            .flatMap(Optional::stream)
            .map(this::toPractitioner)
            .findAny();
    }

    private Practitioner toPractitioner(final RequesterNameAndAddress requester) {
        final var result = new Practitioner();
        result.addIdentifier()
            .setValue(requester.getIdentifier())
            .setSystem(SDS_USER_SYSTEM);
        Optional.ofNullable(requester.getRequesterName())
            .ifPresent(name -> result.addName().setText(name));
        return result;
    }
}
