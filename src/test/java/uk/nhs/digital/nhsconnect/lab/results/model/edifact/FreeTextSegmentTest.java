package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import org.junit.jupiter.api.Test;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

class FreeTextSegmentTest {
    @Test
    void testGetKey() {
        assertThat(new FreeTextSegment(FreeTextType.CLINICAL_INFO, new String[0]).getKey())
            .isEqualTo("FTX");
    }

    @Test
    void testWrongKey() {
        assertThatThrownBy(() -> FreeTextSegment.fromString("WRONG+CID+++OK"))
            .isExactlyInstanceOf(IllegalArgumentException.class)
            .hasMessage("Can't create FreeTextSegment from WRONG+CID+++OK");
    }

    @Test
    void testUnknownQualifier() {
        assertThatThrownBy(() -> FreeTextSegment.fromString("FTX+ZZZ+++Eek"))
            .isExactlyInstanceOf(IllegalArgumentException.class)
            .hasMessage("No free text type for 'ZZZ'");
    }

    @Test
    void testNoTexts() {
        var result = FreeTextSegment.fromString("FTX+CRR+++");
        assertAll(
            () -> assertThat(result.getTexts()).isEmpty(),
            () -> assertThatThrownBy(result::validate)
                .isExactlyInstanceOf(EdifactValidationException.class)
                .hasMessage("FTX+CRR: At least one free text must be given.")
        );
    }

    @Test
    void testOneText() {
        var result = FreeTextSegment.fromString("FTX+RIT+++Okay");
        assertAll(
            () -> assertThat(result.getTexts()).containsExactly("Okay"),
            () -> assertThatNoException().isThrownBy(result::validate)
        );
    }

    @Test
    @SuppressWarnings("checkstyle:MagicNumber")
    void testFromStringTooManyFreeTexts() {
        assertThatThrownBy(() -> FreeTextSegment.fromString("FTX+RPD+++A" + ":A".repeat(10)))
            .isExactlyInstanceOf(IllegalArgumentException.class)
            .hasMessage(
                "Can't create FreeTextSegment from FTX+RPD+++A:A:A:A:A:A:A:A:A:A:A because too many free texts");
    }

    @Test
    @SuppressWarnings("checkstyle:MagicNumber")
    void testPreValidateTooManyFreeTexts() {
        final var texts = "A".repeat(10).split("");
        final var freeText = new FreeTextSegment(FreeTextType.SERVICE_PROVIDER_COMMENT, texts);
        assertThatThrownBy(freeText::validate)
            .isExactlyInstanceOf(EdifactValidationException.class)
            .hasMessage("FTX+SPC: At most 5 free texts may be given.");
    }
}
