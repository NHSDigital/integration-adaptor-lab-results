package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import org.junit.jupiter.api.Test;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

class InvestigationResultFreeTextTest {
    @Test
    void testGetKey() {
        assertThat(new InvestigationResultFreeText().getKey())
            .isEqualTo("FTX");
    }

    @Test
    void testWrongKey() {
        assertThatThrownBy(() -> InvestigationResultFreeText.fromString("WRONG+RIT+++OK"))
            .isExactlyInstanceOf(IllegalArgumentException.class)
            .hasMessage("Can't create InvestigationResultFreeText (FTX+RIT) from WRONG+RIT+++OK");
    }

    @Test
    void testNoTexts() {
        var result = InvestigationResultFreeText.fromString("FTX+RIT+++");
        assertAll(
            () -> assertThat(result.getValue()).isEqualTo("RIT+++"),
            () -> assertThat(result.getTexts()).isEmpty(),
            () -> assertThatThrownBy(result::preValidate)
                .isExactlyInstanceOf(EdifactValidationException.class)
                .hasMessage("FTX+RIT: At least one free text must be given."),
            () -> assertThatNoException().isThrownBy(result::validateStateful)
        );
    }

    @Test
    void testOneText() {
        var result = InvestigationResultFreeText.fromString("FTX+RIT+++Okay");
        assertAll(
            () -> assertThat(result.getValue()).isEqualTo("RIT+++Okay"),
            () -> assertThat(result.getTexts()).containsExactly("Okay"),
            () -> assertThatNoException().isThrownBy(result::preValidate),
            () -> assertThatNoException().isThrownBy(result::validateStateful)
        );
    }

    @Test
    @SuppressWarnings("checkstyle:MagicNumber")
    void testFromStringTooManyFreeTexts() {
        assertThatThrownBy(() -> InvestigationResultFreeText.fromString("FTX+RIT+++A" + ":A".repeat(10)))
            .isExactlyInstanceOf(IllegalArgumentException.class)
            .hasMessage("Can't create InvestigationResultFreeText (FTX+RIT) "
                + "from FTX+RIT+++A:A:A:A:A:A:A:A:A:A:A because too many free texts");
    }

    @Test
    @SuppressWarnings("checkstyle:MagicNumber")
    void testPreValidateTooManyFreeTexts() {
        final var texts = "A".repeat(10).split("");
        final var freeText = new InvestigationResultFreeText(texts);
        assertThatThrownBy(freeText::preValidate)
            .isExactlyInstanceOf(EdifactValidationException.class)
            .hasMessage("FTX+RIT: At most 5 free texts may be given.");
    }
}
