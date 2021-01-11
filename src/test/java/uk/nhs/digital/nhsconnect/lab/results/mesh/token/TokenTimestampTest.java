package uk.nhs.digital.nhsconnect.lab.results.mesh.token;

import org.junit.jupiter.api.Test;
import uk.nhs.digital.nhsconnect.lab.results.utils.TimestampService;

import java.time.Instant;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class TokenTimestampTest {

    private static final Instant FIXED_TIME_LOCAL = ZonedDateTime.of(1991, 11, 6, 12, 30, 0, 0, TimestampService.UKZone)
            .toInstant();

    @Test
    void testTimestampIsInCorrectFormat() {
        final String formattedDateTime = new TokenTimestamp(FIXED_TIME_LOCAL).getValue();
        assertThat(formattedDateTime).isEqualTo("199111061230"); //yyyyMMddHHmm
    }
}
