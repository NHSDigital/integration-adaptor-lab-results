package uk.nhs.digital.nhsconnect.lab.results.translator.mapper;

import lombok.RequiredArgsConstructor;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.digital.nhsconnect.lab.results.model.FhirProfiles;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Message;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.segmentgroup.InvolvedParty;
import uk.nhs.digital.nhsconnect.lab.results.utils.UUIDGenerator;

import java.util.Optional;

import static uk.nhs.digital.nhsconnect.lab.results.model.enums.ServiceProviderCode.PROFESSIONAL;

@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class PractitionerMapper {
    private final UUIDGenerator uuidGenerator;

    public Practitioner mapToRequestingPractitioner(final Message message) {
        return message.getInvolvedParties().stream()
            .filter(party -> party.getServiceProvider().getServiceProviderCode().equals(PROFESSIONAL))
            .map(InvolvedParty::getRequesterNameAndAddress)
            .flatMap(Optional::stream)
            .map(r -> mapToPractitioner(r.getIdentifier().orElse(null), r.getName().orElse(null)))
            .findFirst()
            .orElseGet(this::buildBarePractitioner);
    }

    public Practitioner mapToPerformingPractitioner(final Message message) {
        return message.getInvolvedParties().stream()
            .filter(party -> party.getServiceProvider().getServiceProviderCode().equals(PROFESSIONAL))
            .map(InvolvedParty::getPerformerNameAndAddress)
            .flatMap(Optional::stream)
            .map(p -> mapToPractitioner(p.getIdentifier(), p.getName()))
            .findFirst()
            .orElseGet(this::buildBarePractitioner);
    }

    private Practitioner mapToPractitioner(final String identifier, final String name) {
        final var practitioner = buildBarePractitioner();

        Optional.ofNullable(identifier)
            .ifPresent(id -> practitioner.addIdentifier().setValue(id));

        Optional.ofNullable(name)
            .ifPresent(n -> practitioner.addName().setText(n));

        return practitioner;
    }

    private Practitioner buildBarePractitioner() {
        final var practitioner = new Practitioner();
        practitioner.setId(uuidGenerator.generateUUID());
        practitioner.getMeta().addProfile(FhirProfiles.PRACTITIONER);
        return practitioner;
    }
}
