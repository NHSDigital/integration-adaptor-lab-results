package uk.nhs.digital.nhsconnect.lab.segments.model.edifact;

import lombok.NonNull;
import org.junit.jupiter.api.Test;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.FreeTextSegment;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

class FreeTextSegmentTest {
    private static class SampleFreeText extends FreeTextSegment {
        SampleFreeText(@NonNull final String qualifier, @NonNull final String... texts) {
            super(qualifier, texts);
        }
        static String[] extractSegments(String edifact) {
            return FreeTextSegment.extractFreeTextsFromString(edifact, "FTX+SMP", "ClassName");
        }
    }

    @Test
    void testNoTexts() {
        var segments = SampleFreeText.extractSegments("FTX+SMP+++");
        assertThat(segments).isEmpty();
    }

    @Test
    void testOneText() {
        var segments = SampleFreeText.extractSegments("FTX+SMP+++Okay");
        assertThat(segments).containsExactly("Okay");
    }

    @Test
    @SuppressWarnings("checkstyle:MagicNumber")
    void testFromStringTooManyFreeTexts() {
        assertThatThrownBy(() -> SampleFreeText.extractSegments("FTX+SMP+++A" + ":A".repeat(10)))
            .isExactlyInstanceOf(IllegalArgumentException.class)
            .hasMessage("Can't create ClassName (FTX+SMP) "
                + "from FTX+SMP+++A:A:A:A:A:A:A:A:A:A:A because too many free texts");
    }

    @Test
    @SuppressWarnings("checkstyle:MagicNumber")
    void testPreValidateTooManyFreeTexts() {
        final var texts = "A".repeat(10).split("");
        final var freeText = new SampleFreeText("SMP", texts);
        assertThatThrownBy(freeText::preValidate)
            .isExactlyInstanceOf(EdifactValidationException.class)
            .hasMessage("FTX+SMP: At most 5 free texts may be given.");
    }

    @Test
    void testEqualsAndHashcode() {
        final SampleFreeText a = new SampleFreeText("SMP", "");
        final SampleFreeText b = new SampleFreeText("SMP", "");
        assertAll(
            () -> assertThat(a).isEqualTo(b),
            () -> assertThat(a.hashCode()).isEqualTo(b.hashCode())
        );
    }
}
