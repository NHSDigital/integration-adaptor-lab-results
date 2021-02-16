package uk.nhs.digital.nhsconnect.lab.results.translator.mapper;

import org.hl7.fhir.dstu3.model.Organization;
import org.springframework.stereotype.Component;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Message;

@Component
public class OrganizationMapper {
    public Organization mapToPerformingOrganization(final Message message) {
        return new Organization();
    }

    public Organization mapToRequestingOrganization(final Message message) {
        return new Organization();
    }

    public Organization mapToSpecimenCollectingOrganization(final Message message) {
        return new Organization();
    }
}
