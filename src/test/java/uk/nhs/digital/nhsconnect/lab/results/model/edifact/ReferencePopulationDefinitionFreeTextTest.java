package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import org.junit.jupiter.api.Test;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

class ReferencePopulationDefinitionFreeTextTest {
    @Test
    void testGetKey() {
        assertThat(new ReferencePopulationDefinitionFreeText(new String[0]).getKey())
            .isEqualTo("FTX");
    }

    @Test
    void testWrongKey() {
        assertThatThrownBy(() -> ReferencePopulationDefinitionFreeText.fromString("WRONG+RPD+++OK"))
            .isExactlyInstanceOf(IllegalArgumentException.class)
            .hasMessage("Can't create ReferencePopulationDefinitionFreeText (FTX+RPD) from WRONG+RPD+++OK");
    }

    @Test
    void testNoTexts() {
        var result = ReferencePopulationDefinitionFreeText.fromString("FTX+RPD+++");
        assertAll(
            () -> assertThat(result.getValue()).isEqualTo("RPD+++"),
            () -> assertThat(result.getFreeTexts()).isEmpty(),
            () -> assertThatThrownBy(result::preValidate)
                .isExactlyInstanceOf(EdifactValidationException.class)
                .hasMessage("FTX+RPD: At least one reference population definition must be given."),
            () -> assertThatNoException().isThrownBy(result::validateStateful)
        );
    }

    @Test
    void testOneText() {
        var result = ReferencePopulationDefinitionFreeText.fromString("FTX+RPD+++Okay");
        assertAll(
            () -> assertThat(result.getValue()).isEqualTo("RPD+++Okay"),
            () -> assertThat(result.getFreeTexts()).containsExactly("Okay"),
            () -> assertThatNoException().isThrownBy(result::preValidate),
            () -> assertThatNoException().isThrownBy(result::validateStateful)
        );
    }

    @Test
    @SuppressWarnings("checkstyle:MagicNumber")
    void testFromStringTooManyFreeTexts() {
        assertThatThrownBy(() -> ReferencePopulationDefinitionFreeText.fromString("FTX+RPD+++A" + ":A".repeat(10)))
            .isExactlyInstanceOf(IllegalArgumentException.class)
            .hasMessage("Can't create ReferencePopulationDefinitionFreeText (FTX+RPD) "
                + "from FTX+RPD+++A:A:A:A:A:A:A:A:A:A:A because too many free texts");
    }

    @Test
    @SuppressWarnings("checkstyle:MagicNumber")
    void testPreValidateTooManyFreeTexts() {
        final var texts = "A".repeat(10).split("");
        final var freeText = new ReferencePopulationDefinitionFreeText(texts);
        assertThatThrownBy(freeText::preValidate)
            .isExactlyInstanceOf(EdifactValidationException.class)
            .hasMessage("FTX+RPD: At most 5 reference population definitions may be given.");
    }
}
