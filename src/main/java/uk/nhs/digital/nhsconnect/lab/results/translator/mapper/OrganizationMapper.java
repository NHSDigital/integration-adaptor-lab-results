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

import java.util.List;
import java.util.Optional;

import static uk.nhs.digital.nhsconnect.lab.results.model.edifact.ServiceProviderCode.DEPARTMENT;
import static uk.nhs.digital.nhsconnect.lab.results.model.edifact.ServiceProviderCode.ORGANISATION;

@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class OrganizationMapper {

    private final UUIDGenerator uuidGenerator;

    public Optional<Organization> mapToRequestingOrganization(final Message message) {
        return message.getInvolvedParties().stream()
            .filter(party -> party.getServiceProvider().getServiceProviderCode().equals(ORGANISATION))
            .map(InvolvedParty::getRequesterNameAndAddress)
            .flatMap(Optional::stream)
            .findFirst()
            .map(requester -> mapToOrganization(requester.getRequestingOrganizationName(), null));
    }

    public Optional<Organization> mapToPerformingOrganization(final Message message) {
        String performingOrganizationName = message.getInvolvedParties().stream()
            .filter(party -> party.getServiceProvider().getServiceProviderCode().equals(ORGANISATION))
            .map(InvolvedParty::getPerformerNameAndAddress)
            .flatMap(Optional::stream)
            .map(PerformerNameAndAddress::getPerformingOrganisationName)
            .findFirst()
            .orElse(null);

        if (performingOrganizationName != null) {
            String performingOrganizationDepartment = message.getInvolvedParties().stream()
                .filter(party -> party.getServiceProvider().getServiceProviderCode().equals(DEPARTMENT))
                .map(InvolvedParty::getPerformerNameAndAddress)
                .flatMap(Optional::stream)
                .map(PerformerNameAndAddress::getPerformingOrganisationName)
                .findFirst()
                .orElse(null);

            return Optional.of(mapToOrganization(performingOrganizationName, performingOrganizationDepartment));
        }

        return Optional.empty();
    }

    private Organization mapToOrganization(final String organizationName, final String departmentName) {
        final var organization = new Organization();

        organization.setId(uuidGenerator.generateUUID());
        organization.setName(organizationName);

        if (!StringUtils.isBlank(departmentName)) {
            Coding coding = new Coding()
                .setCode(OrganizationType.DEPT.toCode())
                .setDisplay(departmentName);
            CodeableConcept type = new CodeableConcept().setCoding(List.of(coding));
            organization.setType(List.of(type));
        }

        return organization;
    }
}
