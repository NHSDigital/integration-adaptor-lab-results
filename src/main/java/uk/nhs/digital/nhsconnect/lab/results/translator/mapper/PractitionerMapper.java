package uk.nhs.digital.nhsconnect.lab.results.translator.mapper;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Message;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.segmentgroup.InvolvedParty;
import uk.nhs.digital.nhsconnect.lab.results.utils.UUIDGenerator;

@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class PractitionerMapper {
    private final UUIDGenerator uuidGenerator;

    private static final String SDS_USER_SYSTEM = "https://fhir.nhs.uk/Id/sds-user-id";

    public Optional<Practitioner> mapToRequestingPractitioner(final Message message) {
        return message.getInvolvedParties().stream()
            .map(InvolvedParty::getRequesterNameAndAddress)
            .flatMap(Optional::stream)
            .map(r -> mapToPractitioner(r.getIdentifier(), r.getPractitionerName()))
            .findAny();
    }

    public Optional<Practitioner> mapToPerformingPractitioner(final Message message) {
        return message.getInvolvedParties().stream()
            .map(InvolvedParty::getPerformerNameAndAddress)
            .flatMap(Optional::stream)
            .filter(p -> !StringUtils.isBlank(p.getIdentifier()))
            .map(p -> mapToPractitioner(p.getIdentifier(), p.getPractitionerName()))
            .findAny();
    }

    private Practitioner mapToPractitioner(final String identifier, final String name) {
        final var result = new Practitioner();

        result.addIdentifier()
            .setValue(identifier)
            .setSystem(SDS_USER_SYSTEM);
        Optional.ofNullable(name)
            .ifPresent(n -> result.addName().setText(n));
        result.setId(uuidGenerator.generateUUID());

        return result;
    }
}
