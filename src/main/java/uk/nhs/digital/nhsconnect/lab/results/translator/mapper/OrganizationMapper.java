package uk.nhs.digital.nhsconnect.lab.results.translator.mapper;

import lombok.RequiredArgsConstructor;
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

import static uk.nhs.digital.nhsconnect.lab.results.model.enums.ServiceProviderCode.DEPARTMENT;
import static uk.nhs.digital.nhsconnect.lab.results.model.enums.ServiceProviderCode.ORGANIZATION;

@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class OrganizationMapper {
    private static final String ODS_ORGANIZATION_SYSTEM = "https://fhir.nhs.uk/Id/ods-organization-code";

    private final UUIDGenerator uuidGenerator;

    public Optional<Organization> mapToRequestingOrganization(final Message message) {
        return message.getInvolvedParties().stream()
            .filter(party -> party.getServiceProvider().getServiceProviderCode() == ORGANIZATION)
            .map(InvolvedParty::getRequesterNameAndAddress)
            .flatMap(Optional::stream)
            .findFirst()
            .map(requester -> {
                final var organization = new Organization();
                organization.getMeta().addProfile(FhirProfiles.ORGANIZATION);
                organization.setId(uuidGenerator.generateUUID());
                requester.getName().map(MappingUtils::unescape).ifPresent(organization::setName);
                requester.getIdentifier().ifPresent(id -> organization.addIdentifier()
                    .setSystem(ODS_ORGANIZATION_SYSTEM)
                    .setValue(id));
                return organization;
            });
    }

    public Optional<Organization> mapToPerformingOrganization(final Message message) {
        final var performingOrganization = message.getInvolvedParties().stream()
            .filter(party -> party.getServiceProvider().getServiceProviderCode() == ORGANIZATION)
            .filter(organization -> organization.getPerformerNameAndAddress().isPresent())
            .findAny();

        final var performingDepartment = message.getInvolvedParties().stream()
            .filter(party -> party.getServiceProvider().getServiceProviderCode() == DEPARTMENT)
            .filter(department -> department.getPerformerNameAndAddress().isPresent())
            .findAny();

        if (performingOrganization.isEmpty() && performingDepartment.isEmpty()) {
            return Optional.empty();
        }

        final var organization = new Organization();
        organization.getMeta().addProfile(FhirProfiles.ORGANIZATION);
        organization.setId(uuidGenerator.generateUUID());

        final Optional<PerformerNameAndAddress> performerNameAndAddress = performingOrganization
            .flatMap(InvolvedParty::getPerformerNameAndAddress);

        performerNameAndAddress
            .map(PerformerNameAndAddress::getName)
            .map(MappingUtils::unescape)
            .ifPresent(organization::setName);

        performerNameAndAddress.map(PerformerNameAndAddress::getIdentifier)
            .ifPresent(id -> organization.addIdentifier()
                .setSystem(ODS_ORGANIZATION_SYSTEM)
                .setValue(id));

        performingDepartment.flatMap(InvolvedParty::getPerformerNameAndAddress)
            .map(PerformerNameAndAddress::getName)
            .map(departmentName -> new Coding()
                .setCode(OrganizationType.DEPT.toCode())
                .setDisplay(departmentName))
            .map(coding -> new CodeableConcept().addCoding(coding))
            .ifPresent(organization::addType);

        return Optional.of(organization);
    }
}
