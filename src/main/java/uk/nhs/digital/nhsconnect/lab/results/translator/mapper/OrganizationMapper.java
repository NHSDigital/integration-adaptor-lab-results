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
import java.util.stream.Collectors;

import static uk.nhs.digital.nhsconnect.lab.results.model.enums.ServiceProviderCode.DEPARTMENT;
import static uk.nhs.digital.nhsconnect.lab.results.model.enums.ServiceProviderCode.ORGANIZATION;

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
            .map(requester -> mapToOrganization(requester.getName(), null));
    }

    public Optional<Organization> mapToPerformingOrganization(final Message message) {
        List<InvolvedParty> organizationList = message.getInvolvedParties().stream()
            .filter(party -> party.getServiceProvider().getServiceProviderCode().equals(ORGANIZATION))
            .collect(Collectors.toList());

        return mapToPerformingPartyName(organizationList).map(organizationName -> {
            List<InvolvedParty> departmentList = message.getInvolvedParties().stream()
                .filter(party -> party.getServiceProvider().getServiceProviderCode().equals(DEPARTMENT))
                .collect(Collectors.toList());

            return mapToPerformingPartyName(departmentList)
                .map(departmentName -> mapToOrganization(organizationName, departmentName))
                .orElseGet(() -> mapToOrganization(organizationName, null));
        });
    }

    private Optional<String> mapToPerformingPartyName(List<InvolvedParty> involvedPartyList) {
        return involvedPartyList.stream().map(InvolvedParty::getPerformerNameAndAddress)
            .flatMap(Optional::stream)
            .findFirst()
            .map(PerformerNameAndAddress::getPartyName);
    }

    private Organization mapToOrganization(final String organizationName, final String departmentName) {
        final var organization = new Organization();

        organization.setId(uuidGenerator.generateUUID());
        organization.setName(organizationName.replaceAll("\\?'", "'"));

        if (!StringUtils.isBlank(departmentName)) {
            final Coding coding = new Coding()
                .setCode(OrganizationType.DEPT.toCode())
                .setDisplay(departmentName);
            CodeableConcept type = new CodeableConcept().addCoding(coding);
            organization.addType(type);
        }

        return organization;
    }
}
