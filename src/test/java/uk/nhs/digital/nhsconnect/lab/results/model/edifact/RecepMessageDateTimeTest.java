package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import org.junit.jupiter.api.Test;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RecepMessageDateTimeTest {
    private static final Instant WINTER_INSTANT = Instant.parse("2020-03-28T20:58:00Z");
    private static final Instant SUMMER_INSTANT = Instant.parse("2020-05-28T20:58:00Z");
    private static final String WINTER_RECEP = "DTM+815:202003282058:306'";
    private static final String SUMMER_RECEP = "DTM+815:202005282158:306'";

    @Test
    void when_toEdifact_and_instantInWinter_expect_edifactIsUTC() throws EdifactValidationException {
        assertThat(new RecepMessageDateTime(WINTER_INSTANT).toEdifact())
            .isEqualTo(WINTER_RECEP);
    }

    @Test
    void when_toEdifact_and_instantInSummer_expect_edifactIsBST() throws EdifactValidationException {
        // the translated times are UK local time / BST and one hour "ahead" of UTC
        assertThat(new RecepMessageDateTime(SUMMER_INSTANT).toEdifact())
            .isEqualTo(SUMMER_RECEP);
    }

    @Test
    void when_fromString_and_edifactIsWinterUTC_expect_instantIsUTC() {
        assertThat(RecepMessageDateTime.fromString(WINTER_RECEP).getTimestamp())
            .isEqualTo(WINTER_INSTANT);
    }

    @Test
    void when_fromString_and_edifactIsSummerBST_expect_instantIsUTC() {
        // the internal Instant representation (UTC) is one hour "behind" the UK local time / BST EDIFACT timestamp
        assertThat(RecepMessageDateTime.fromString(SUMMER_RECEP).getTimestamp())
            .isEqualTo(SUMMER_INSTANT);
    }

    @Test
    void when_fromString_and_stringIsNotDTMSegment_expect_throwsException() {
        assertThatThrownBy(() -> RecepMessageDateTime.fromString("DTM+123:456:789'"))
            .isExactlyInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void when_fromString_and_stringIsNull_expect_throwsException() {
        assertThatThrownBy(() -> RecepMessageDateTime.fromString(null))
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
