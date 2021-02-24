package uk.nhs.digital.nhsconnect.lab.results.translator.mapper;

import org.hl7.fhir.dstu3.model.Practitioner;
import org.springframework.stereotype.Component;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Message;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.segmentgroup.InvolvedParty;

import java.util.Optional;

@Component
public class PractitionerMapper {
    private static final String SDS_USER_SYSTEM = "https://fhir.nhs.uk/Id/sds-user-id";

    public Optional<Practitioner> mapRequester(final Message message) {
        return message.getInvolvedParties().stream()
            .map(InvolvedParty::getRequesterNameAndAddress)
            .flatMap(Optional::stream)
            .map(r -> mapToPractitioner(r.getIdentifier(), r.getRequesterName()))
            .findAny();
    }


    public Optional<Practitioner> mapPerformer(final Message message) {
        return message.getInvolvedParties().stream()
            .map(InvolvedParty::getPerformerNameAndAddress)
            .flatMap(Optional::stream)
            .filter(p -> !p.getIdentifier().isBlank())
            .map(p -> mapToPractitioner(p.getIdentifier(), p.getPerformerName()))
            .findAny();
    }

    private Practitioner mapToPractitioner(final String identifier, final String name) {
        final var result = new Practitioner();

        result.addIdentifier()
            .setValue(identifier)
            .setSystem(SDS_USER_SYSTEM);
        Optional.ofNullable(name)
            .ifPresent(n -> result.addName().setText(n));

        return result;
    }
}
