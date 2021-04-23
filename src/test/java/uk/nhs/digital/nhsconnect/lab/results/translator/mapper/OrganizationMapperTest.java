package uk.nhs.digital.nhsconnect.lab.results.translator.mapper;

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

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.when;

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

        Organization organization = mapper.mapToRequestingOrganization(message);
        assertAll(
            () -> assertThat(organization.getName()).isNull(),
            () -> assertThat(organization.getType()).isEmpty(),
            () -> assertThat(organization.getIdentifier()).isEmpty()
        );
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

        Organization organization = mapper.mapToRequestingOrganization(message);
        assertAll(
            () -> assertThat(organization.getName()).isEqualTo("MATTHEW'S GP"),
            () -> assertThat(organization.getType()).isEmpty(),
            () -> assertThat(organization.getIdentifier()).isEmpty()
        );
    }

    @Test
    void testMapMessageToPerformingOrganizationWithNoPerformerDetails() {
        when(message.getInvolvedParties()).thenReturn(Collections.emptyList());

        Organization organization = mapper.mapToPerformingOrganization(message);
        assertAll(
            () -> assertThat(organization.getName()).isNull(),
            () -> assertThat(organization.getType()).isEmpty(),
            () -> assertThat(organization.getIdentifier()).isEmpty()
        );
    }

    @Test
    void testMapMessageToPerformingOrganizationWithNameCodeAndDepartment() {
        final var performingDepartmentParty = new InvolvedParty(List.of(
            "ignore me",
            "NAD+SLA+++Microbiology",
            "SPR+DPT",
            "ignore me"
        ));

        final var performingOrganizationParty = new InvolvedParty(List.of(
            "ignore me",
            "NAD+SLA+A2442389:902++DR J SMITH",
            "SPR+ORG",
            "ignore me"
        ));

        when(message.getInvolvedParties())
            .thenReturn(List.of(performingDepartmentParty, performingOrganizationParty));

        Organization organization = mapper.mapToPerformingOrganization(message);
        Coding type = organization.getType().get(0).getCoding().get(0);

        assertAll(
            () -> assertThat(organization.getIdentifier()).hasSize(1).first()
                .satisfies(identifier -> assertAll(
                    () -> assertThat(identifier.getValue()).isEqualTo("A2442389"),
                    () -> assertThat(identifier.getSystem())
                        .isEqualTo("https://fhir.nhs.uk/Id/ods-organization-code"))),
            () -> assertThat(organization.getName()).isEqualTo("DR J SMITH"),
            () -> assertThat(type.getCode()).isEqualTo(OrganizationType.DEPT.toCode()),
            () -> assertThat(type.getDisplay()).isEqualTo("Microbiology")
        );
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

        when(message.getInvolvedParties())
            .thenReturn(List.of(performingDepartmentParty, performingOrganizationParty));

        Organization organization = mapper.mapToPerformingOrganization(message);
        Coding type = organization.getType().get(0).getCoding().get(0);

        assertAll(
            () -> assertThat(organization.getName()).isEqualTo("ST JAMES'S UNIVERSITY HOSPITAL"),
            () -> assertThat(organization.getIdentifier()).isEmpty(),
            () -> assertThat(type.getCode()).isEqualTo(OrganizationType.DEPT.toCode()),
            () -> assertThat(type.getDisplay()).isEqualTo("Microbiology")
        );
    }

    @Test
    void testMapMessageToPerformingOrganizationWithCodeOnly() {
        final var performingParty = new InvolvedParty(List.of(
            "ignore me",
            "NAD+SLA+REF00:903",
            "SPR+ORG",
            "ignore me"
        ));

        when(message.getInvolvedParties()).thenReturn(List.of(performingParty));

        Organization organization = mapper.mapToPerformingOrganization(message);
        assertAll(
            () -> assertThat(organization.getIdentifier()).hasSize(1).first()
                .satisfies(identifier -> assertAll(
                    () -> assertThat(identifier.getValue()).isEqualTo("REF00"),
                    () -> assertThat(identifier.getSystem())
                        .isEqualTo("https://fhir.nhs.uk/Id/ods-organization-code"))),
            () -> assertThat(organization.getName()).isNull(),
            () -> assertThat(organization.getType()).isEmpty()
        );
    }

    @Test
    void testMapMessageToPerformingOrganizationWithCodeAndDepartment() {
        final var performingDepartmentParty = new InvolvedParty(List.of(
            "ignore me",
            "NAD+SLA+++Microbiology",
            "SPR+DPT",
            "ignore me"
        ));

        final var performingOrganizationParty = new InvolvedParty(List.of(
            "ignore me",
            "NAD+SLA+REF00:903",
            "SPR+ORG",
            "ignore me"
        ));

        when(message.getInvolvedParties())
            .thenReturn(List.of(performingDepartmentParty, performingOrganizationParty));

        Organization organization = mapper.mapToPerformingOrganization(message);

        Coding type = organization.getType().get(0).getCoding().get(0);

        assertAll(
            () -> assertThat(organization.getIdentifier()).hasSize(1).first()
                .satisfies(identifier -> assertAll(
                    () -> assertThat(identifier.getValue()).isEqualTo("REF00"),
                    () -> assertThat(identifier.getSystem())
                        .isEqualTo("https://fhir.nhs.uk/Id/ods-organization-code"))),
            () -> assertThat(organization.getName()).isNull(),
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

        Organization organization = mapper.mapToPerformingOrganization(message);
        assertAll(
            () -> assertThat(organization.getName()).isEqualTo("ST JAMES'S UNIVERSITY HOSPITAL"),
            () -> assertThat(organization.getType()).isEmpty(),
            () -> assertThat(organization.getIdentifier()).isEmpty()
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

        Organization organization = mapper.mapToPerformingOrganization(message);
        assertAll(
            () -> assertThat(organization.getName()).isNull(),
            () -> assertThat(organization.getType()).isEmpty(),
            () -> assertThat(organization.getIdentifier()).isEmpty()
        );
    }
}
