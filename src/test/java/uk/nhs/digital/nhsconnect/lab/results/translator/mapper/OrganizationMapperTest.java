package uk.nhs.digital.nhsconnect.lab.results.translator.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Organization;
import org.hl7.fhir.dstu3.model.codesystems.OrganizationType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Message;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.segmentgroup.InvolvedParty;
import uk.nhs.digital.nhsconnect.lab.results.utils.UUIDGenerator;

@ExtendWith(MockitoExtension.class)
class OrganizationMapperTest {

    @Mock
    private Message message;

    @Mock
    private UUIDGenerator uuidGenerator;

    @InjectMocks
    private OrganizationMapper mapper;

    @Test
    void testMapMessageToRequestingOrganizationWithNoRequesterDetails() {
        when(message.getInvolvedParties()).thenReturn(Collections.emptyList());

        assertThat(mapper.mapToRequestingOrganization(message)).isEmpty();
    }

    @Test
    void testMapMessageToRequestingOrganization() {
        final var requestingParty = new InvolvedParty(List.of(
            "ignore me",
            "NAD+PO+++MATTHEW?'S GP",
            "SPR+ORG",
            "ignore me"
        ));

        when(message.getInvolvedParties()).thenReturn(List.of(requestingParty));

        Optional<Organization> result = mapper.mapToRequestingOrganization(message);
        assertThat(result).isNotEmpty();

        Organization organization = result.get();

        assertAll(
            () -> assertThat(organization.getName()).isEqualTo("MATTHEW'S GP"),
            () -> assertThat(organization.getType()).isEmpty()
        );
    }

    @Test
    void testMapMessageToPerformingOrganizationWithNoPerformerDetails() {
        when(message.getInvolvedParties()).thenReturn(Collections.emptyList());

        assertThat(mapper.mapToPerformingOrganization(message)).isEmpty();
    }

    @Test
    void testMapMessageToPerformingOrganizationWithOrganizationAndDepartment() {
        final var performingDepartmentParty = new InvolvedParty(List.of(
            "ignore me",
            "NAD+SLA+++Microbiology",
            "SPR+DPT",
            "ignore me"
        ));

        final var performingOrganizationParty = new InvolvedParty(List.of(
            "ignore me",
            "NAD+SLA+++ST JAMES?'S UNIVERSITY HOSPITAL",
            "SPR+ORG",
            "ignore me"
        ));

        when(message.getInvolvedParties()).thenReturn(List.of(performingDepartmentParty, performingOrganizationParty));

        Optional<Organization> result = mapper.mapToPerformingOrganization(message);
        assertThat(result).isNotEmpty();

        Organization organization = result.get();

        Coding type = organization.getType().get(0).getCoding().get(0);
        assertAll(
            () -> assertThat(organization.getName()).isEqualTo("ST JAMES'S UNIVERSITY HOSPITAL"),
            () -> assertThat(type.getCode()).isEqualTo(OrganizationType.DEPT.toCode()),
            () -> assertThat(type.getDisplay()).isEqualTo("Microbiology")
        );
    }

    @Test
    void testMapMessageToPerformingOrganizationWithOrganizationAndNoDepartment() {
        final var performingParty = new InvolvedParty(List.of(
            "ignore me",
            "NAD+SLA+++ST JAMES?'S UNIVERSITY HOSPITAL",
            "SPR+ORG",
            "ignore me"
        ));

        when(message.getInvolvedParties()).thenReturn(List.of(performingParty));

        Optional<Organization> result = mapper.mapToPerformingOrganization(message);
        assertThat(result).isNotEmpty();

        Organization organization = result.get();

        assertAll(
            () -> assertThat(organization.getName()).isEqualTo("ST JAMES'S UNIVERSITY HOSPITAL"),
            () -> assertThat(organization.getType()).isEmpty()
        );
    }

    @Test
    void testMapMessageWherePerformerIsNotOrganization() {
        final var involvedParty = new InvolvedParty(List.of(
            "ignore me",
            "NAD+PO+DEF:900++Some practitioner name",
            "SPR+PRO",
            "ignore me"
        ));

        when(message.getInvolvedParties()).thenReturn(List.of(involvedParty));

        Optional<Organization> result = mapper.mapToPerformingOrganization(message);
        assertThat(result).isEmpty();
    }
}
