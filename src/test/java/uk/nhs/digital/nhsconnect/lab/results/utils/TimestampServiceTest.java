package uk.nhs.digital.nhsconnect.lab.results.utils;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class TimestampServiceTest {
    @Test
    void whenGettingTimestamp_thenPrecisionIsMilliseconds() {
        final var instant = new TimestampService().getCurrentTimestamp();
        final long remainder = instant.getNano() % TimeUnit.MILLISECONDS.toNanos(1);

        assertThat(remainder).isEqualTo(0);
    }

    @Test
    void whenFormattingInISO_thenISOForUKZoneIsReturned() {
        final Instant timestamp = Instant.ofEpochSecond(123123);

        assertThat(new TimestampService().formatInISO(timestamp)).isEqualTo("1970-01-02T11:12:03+01:00[Europe/London]");
    }
}
