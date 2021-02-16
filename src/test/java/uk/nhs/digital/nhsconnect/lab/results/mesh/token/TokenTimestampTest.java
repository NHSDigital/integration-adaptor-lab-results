package uk.nhs.digital.nhsconnect.lab.results.mesh.token;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class TokenTimestampTest {

    private static final Instant FIXED_TIME_LOCAL = ZonedDateTime.parse("1991-11-06T12:30:00.000Z").toInstant();

    @Test
    void testTimestampIsInCorrectFormat() {
        final String formattedDateTime = new TokenTimestamp(FIXED_TIME_LOCAL).getValue();
        assertThat(formattedDateTime).isEqualTo("199111061230"); //yyyyMMddHHmm
    }
}
