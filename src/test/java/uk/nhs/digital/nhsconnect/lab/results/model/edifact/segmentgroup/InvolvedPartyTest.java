package uk.nhs.digital.nhsconnect.lab.results.model.edifact.segmentgroup;

import org.junit.jupiter.api.Test;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.MessageRecipientNameAndAddress;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.PerformingOrganisationNameAndAddress;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.Reference;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.RequesterNameAndAddress;

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
        assertThat(involvedParty.getPerformingOrganisationNameAndAddress())
            .isPresent()
            .map(PerformingOrganisationNameAndAddress::getValue)
            .contains("SLA+++ST JAMES?'S UNIVERSITY HOSPITAL");
    }

    @Test
    void testGetRequesterNameAndAddress() {
        final var involvedParty = new InvolvedParty(List.of(
            "ignore me",
            "NAD+PO+G3380314:900++SCOTT",
            "ignore me"
        ));
        assertThat(involvedParty.getRequesterNameAndAddress())
            .isPresent()
            .map(RequesterNameAndAddress::getValue)
            .contains("PO+G3380314:900++SCOTT");
    }

    @Test
    void testGetMessageRecipientNameAndAddress() {
        final var involvedParty = new InvolvedParty(List.of(
            "ignore me",
            "NAD+MR+G3380314:900++SCOTT",
            "ignore me"
        ));
        assertThat(involvedParty.getMessageRecipientNameAndAddress())
            .isPresent()
            .map(MessageRecipientNameAndAddress::getValue)
            .contains("MR+G3380314:900++SCOTT");
    }

    @Test
    void testGetPartnerAgreedIdentification() {
        final var involvedParty = new InvolvedParty(List.of(
            "ignore me",
            "RFF+AHI:agreed ID",
            "ignore me"
        ));
        assertThat(involvedParty.getPartnerAgreedIdentification())
            .isPresent()
            .map(Reference::getValue)
            .contains("AHI:agreed ID");
    }

    @Test
    void testGetServiceProvider() {
        final var involvedParty = new InvolvedParty(List.of(
            "ignore me",
            "SPR+ORG",
            "ignore me"
        ));
        assertThat(involvedParty.getServiceProvider().getValue())
            .isEqualTo("ORG");
    }
}
