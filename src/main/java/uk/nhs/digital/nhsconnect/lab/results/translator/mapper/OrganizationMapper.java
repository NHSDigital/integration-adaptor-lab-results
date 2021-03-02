package uk.nhs.digital.nhsconnect.lab.results.translator.mapper;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Organization;
import org.hl7.fhir.dstu3.model.codesystems.OrganizationType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Message;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.PerformerNameAndAddress;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.segmentgroup.InvolvedParty;
import uk.nhs.digital.nhsconnect.lab.results.utils.UUIDGenerator;

import java.util.Optional;
import java.util.stream.Stream;

import static uk.nhs.digital.nhsconnect.lab.results.model.edifact.ServiceProviderCode.DEPARTMENT;
import static uk.nhs.digital.nhsconnect.lab.results.model.edifact.ServiceProviderCode.ORGANIZATION;

@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class OrganizationMapper {

    private final UUIDGenerator uuidGenerator;

    public Optional<Organization> mapToRequestingOrganization(final Message message) {
        return message.getInvolvedParties().stream()
            .filter(party -> party.getServiceProvider().getServiceProviderCode().equals(ORGANIZATION))
            .map(InvolvedParty::getRequesterNameAndAddress)
            .flatMap(Optional::stream)
            .findFirst()
            .map(requester -> mapToOrganization(requester.getOrganizationName(), null));
    }

    public Optional<Organization> mapToPerformingOrganization(final Message message) {
        Stream<InvolvedParty> organizationStream = message.getInvolvedParties().stream()
            .filter(party -> party.getServiceProvider().getServiceProviderCode().equals(ORGANIZATION));

        String performingOrganizationName = mapToPerformerName(organizationStream);

        if (performingOrganizationName != null) {
            Stream<InvolvedParty> departmentStream = message.getInvolvedParties().stream()
                .filter(party -> party.getServiceProvider().getServiceProviderCode().equals(DEPARTMENT));

            String performingOrganizationDepartmentName = mapToPerformerName(departmentStream);

            return Optional.of(mapToOrganization(performingOrganizationName, performingOrganizationDepartmentName));
        }

        return Optional.empty();
    }

    private String mapToPerformerName(Stream<InvolvedParty> involvedPartyStream) {
        return involvedPartyStream.map(InvolvedParty::getPerformerNameAndAddress)
            .flatMap(Optional::stream)
            .map(PerformerNameAndAddress::getOrganizationName)
            .findFirst()
            .orElse(null);
    }

    private Organization mapToOrganization(final String organizationName, final String departmentName) {
        final var organization = new Organization();

        organization.setId(uuidGenerator.generateUUID());
        organization.setName(organizationName);

        if (!StringUtils.isBlank(departmentName)) {
            Coding coding = new Coding()
                .setCode(OrganizationType.DEPT.toCode())
                .setDisplay(departmentName);
            CodeableConcept type = new CodeableConcept().addCoding(coding);
            organization.addType(type);
        }

        return organization;
    }
}
