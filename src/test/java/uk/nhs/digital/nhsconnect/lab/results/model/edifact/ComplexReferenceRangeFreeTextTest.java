package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import org.junit.jupiter.api.Test;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

class ComplexReferenceRangeFreeTextTest {
    @Test
    void testGetKey() {
        assertThat(new ComplexReferenceRangeFreeText().getKey())
            .isEqualTo("FTX");
    }

    @Test
    void testWrongKey() {
        assertThatThrownBy(() -> ComplexReferenceRangeFreeText.fromString("WRONG+CRR+++OK"))
            .isExactlyInstanceOf(IllegalArgumentException.class)
            .hasMessage("Can't create ComplexReferenceRangeFreeText (FTX+CRR) from WRONG+CRR+++OK");
    }

    @Test
    void testNoTexts() {
        var result = ComplexReferenceRangeFreeText.fromString("FTX+CRR+++");
        assertAll(
            () -> assertThat(result.getValue()).isEqualTo("CRR+++"),
            () -> assertThat(result.getTexts()).isEmpty(),
            () -> assertThatThrownBy(result::preValidate)
                .isExactlyInstanceOf(EdifactValidationException.class)
                .hasMessage("FTX+CRR: At least one free text must be given."),
            () -> assertThatNoException().isThrownBy(result::validateStateful)
        );
    }

    @Test
    void testOneText() {
        var result = ComplexReferenceRangeFreeText.fromString("FTX+CRR+++Okay");
        assertAll(
            () -> assertThat(result.getValue()).isEqualTo("CRR+++Okay"),
            () -> assertThat(result.getTexts()).containsExactly("Okay"),
            () -> assertThatNoException().isThrownBy(result::preValidate),
            () -> assertThatNoException().isThrownBy(result::validateStateful)
        );
    }

    @Test
    @SuppressWarnings("checkstyle:MagicNumber")
    void testFromStringTooManyFreeTexts() {
        assertThatThrownBy(() -> ComplexReferenceRangeFreeText.fromString("FTX+CRR+++A" + ":A".repeat(10)))
            .isExactlyInstanceOf(IllegalArgumentException.class)
            .hasMessage("Can't create ComplexReferenceRangeFreeText (FTX+CRR) "
                + "from FTX+CRR+++A:A:A:A:A:A:A:A:A:A:A because too many free texts");
    }

    @Test
    @SuppressWarnings("checkstyle:MagicNumber")
    void testPreValidateTooManyFreeTexts() {
        final var texts = "A".repeat(10).split("");
        final var freeText = new ComplexReferenceRangeFreeText(texts);
        assertThatThrownBy(freeText::preValidate)
            .isExactlyInstanceOf(EdifactValidationException.class)
            .hasMessage("FTX+CRR: At most 5 free texts may be given.");
    }
}
