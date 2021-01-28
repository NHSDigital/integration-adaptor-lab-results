package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class RecepHeaderTest {
    @Test
    void toEdifactTest() {
        final var dateTime = Instant.parse("2019-03-23T09:00:00Z");
        final var recepHeader = new RecepHeader("SNDR", "RECP", dateTime).setSequenceNumber(1L);

        final String edifact = recepHeader.toEdifact();

        assertThat(edifact).isEqualTo("UNB+UNOA:2+SNDR+RECP+190323:0900+00000001++RECEP+++EDIFACT TRANSFER'");
    }
}
