package uk.nhs.digital.nhsconnect.lab.results.model.edifact.segmentgroup;

import org.junit.jupiter.api.Test;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.HealthcareRegistrationIdentificationCode;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.MessageRecipientNameAndAddress;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.PerformingOrganisationNameAndAddress;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Reference;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.ReferenceType;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.RequesterNameAndAddress;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.ServiceProviderCode;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class InvolvedPartyTest {
    @Test
    void testIndicator() {
        assertThat(InvolvedParty.INDICATOR).isEqualTo("S01");
    }

    @Test
    void testGetPerformingOrganisationNameAndAddress() {
        final var involvedParty = new InvolvedParty(List.of(
            "ignore me",
            "NAD+SLA+++ST JAMES?'S UNIVERSITY HOSPITAL",
            "ignore me"
        ));
        assertThat(involvedParty.getOrganisationNameAndAddress())
            .isPresent()
            .map(PerformingOrganisationNameAndAddress::getPerformingOrganisationName)
            .contains("ST JAMES?'S UNIVERSITY HOSPITAL");
    }

    @Test
    void testGetRequesterNameAndAddress() {
        final var involvedParty = new InvolvedParty(List.of(
            "ignore me",
            "NAD+PO+G3380314:900++SCOTT",
            "ignore me"
        ));
        var requesterNameAndAddress = involvedParty.getRequesterNameAndAddress();
        assertThat(requesterNameAndAddress)
            .isPresent()
            .map(RequesterNameAndAddress::getRequesterName)
            .hasValue("SCOTT");
        assertThat(requesterNameAndAddress)
            .isPresent()
            .map(RequesterNameAndAddress::getHealthcareRegistrationIdentificationCode)
            .hasValue(HealthcareRegistrationIdentificationCode.GP);
        assertThat(requesterNameAndAddress)
            .isPresent()
            .map(RequesterNameAndAddress::getIdentifier)
            .hasValue("G3380314");
    }

    @Test
    void testGetMessageRecipientNameAndAddress() {
        final var involvedParty = new InvolvedParty(List.of(
            "ignore me",
            "NAD+MR+G3380314:900++SCOTT",
            "ignore me"
        ));
        var recipientNameAndAddress = involvedParty.getRecipientNameAndAddress();
        assertThat(recipientNameAndAddress)
            .isPresent()
            .map(MessageRecipientNameAndAddress::getMessageRecipientName)
            .hasValue("SCOTT");
        assertThat(recipientNameAndAddress)
            .isPresent()
            .map(MessageRecipientNameAndAddress::getHealthcareRegistrationIdentificationCode)
            .hasValue(HealthcareRegistrationIdentificationCode.GP);
        assertThat(recipientNameAndAddress)
            .isPresent()
            .map(MessageRecipientNameAndAddress::getIdentifier)
            .hasValue("G3380314");
    }

    @Test
    void testGetPartnerAgreedIdentification() {
        final var involvedParty = new InvolvedParty(List.of(
            "ignore me",
            "RFF+AHI:agreed ID",
            "ignore me"
        ));
        var partnerAgreedId = involvedParty.getPartnerAgreedId();
        assertThat(partnerAgreedId)
            .isPresent()
            .map(Reference::getTarget)
            .map(ReferenceType::getQualifier)
            .contains("AHI");
        assertThat(partnerAgreedId)
            .isPresent()
            .map(Reference::getNumber)
            .contains("agreed ID");
    }

    @Test
    void testGetServiceProvider() {
        final var involvedParty = new InvolvedParty(List.of(
            "ignore me",
            "SPR+ORG",
            "ignore me"
        ));
        assertThat(involvedParty.getServiceProvider().getServiceProviderCode())
            .isEqualTo(ServiceProviderCode.ORGANISATION);
    }
}
