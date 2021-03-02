package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import org.junit.jupiter.api.Test;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.segmentgroup.InvolvedParty;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.segmentgroup.ServiceReportDetails;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class MessageTest {
    @Test
    void testGetHealthAuthorityNameAndAddress() {
        final Message msg = new Message(List.of(
                "NAD+FHS+XX1:954"
        ));

        HealthAuthorityNameAndAddress haAddress = msg.getHealthAuthorityNameAndAddress();

        assertAll(
                () -> assertEquals("NAD", haAddress.getKey()),
                () -> assertEquals("954", haAddress.getCode()),
                () -> assertEquals("XX1", haAddress.getIdentifier())
        );
    }

    @Test
    void testFindFirstGpCodeDefaultsTo9999() {
        final Message msg = new Message(List.of());

        String firstGpCode = msg.findFirstGpCode();

        assertEquals("9999", firstGpCode);
    }

    @Test
    void testFindFirstGpCodeReturnsCorrectly() {
        final Message msg = new Message(List.of(
                "NAD+GP+2750922,295:900",
                "NAD+GP+1649811,184:899"
        ));

        String firstGpCode = msg.findFirstGpCode();

        assertEquals("2750922,295", firstGpCode);
    }

    @Test
    void testGetInvolvedParties() {
        final var msg = new Message(List.of(
                "ignore me",
                "S01+01",
                "party #1 contents",
                "S01+01",
                "party #2 contents",
                "S02+02",
                "ignore me"
        ));
        assertAll(
                () -> assertThat(msg.getInvolvedParties()).hasSize(2)
                        .first()
                        .extracting(InvolvedParty::getEdifactSegments)
                        .isEqualTo(List.of("S01+01", "party #1 contents")),
                () -> assertThat(msg.getInvolvedParties())
                        .last()
                        .extracting(InvolvedParty::getEdifactSegments)
                        .isEqualTo(List.of("S01+01", "party #2 contents"))
        );
    }

    @Test
    void testGetServiceReportDetails() {
        final var msg = new Message(List.of(
                "ignore me",
                "S02+02",
                "service report contents",
                "UNT+18+00000003",
                "ignore me"
        ));
        assertThat(msg.getServiceReportDetails())
                .isNotNull()
                .extracting(ServiceReportDetails::getEdifactSegments)
                .isEqualTo(List.of("S02+02", "service report contents"));
    }
}
