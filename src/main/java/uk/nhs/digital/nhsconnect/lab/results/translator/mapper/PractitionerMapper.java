package uk.nhs.digital.nhsconnect.lab.results.translator.mapper;

import org.hl7.fhir.dstu3.model.Practitioner;
import org.springframework.stereotype.Component;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Message;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.PerformerNameAndAddress;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.RequesterNameAndAddress;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.segmentgroup.InvolvedParty;

import java.util.Optional;

@Component
public class PractitionerMapper {
    private static final String SDS_USER_SYSTEM = "https://fhir.nhs.uk/Id/sds-user-id";

    public Optional<Practitioner> mapRequester(final Message message) {
        Optional<RequesterNameAndAddress> requester = message.getInvolvedParties().stream()
            .map(InvolvedParty::getRequesterNameAndAddress)
            .flatMap(Optional::stream)
            .findAny();

        if (requester.isPresent()) {
            final var r = requester.get();
            Practitioner practitioner = mapToPractitioner(r.getIdentifier(), r.getRequesterName());
            return Optional.of(practitioner);
        } else {
            return Optional.empty();
        }
    }


    public Optional<Practitioner> mapPerformer(final Message message) {
        Optional<PerformerNameAndAddress> performer = message.getInvolvedParties().stream()
            .map(InvolvedParty::getPerformerNameAndAddress)
            .flatMap(Optional::stream)
            .findAny();

        if (performer.isPresent()) {
            final var p = performer.get();
            Practitioner practitioner = mapToPractitioner(p.getIdentifier(), p.getPerformerName());
            return Optional.of(practitioner);
        } else {
            return Optional.empty();
        }
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
