package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import org.apache.commons.lang3.StringUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class SpecimenQuantityTest {
    @Test
    void toEdifactTest() {
        var edifact = new SpecimenQuantity(1750, "mL").toEdifact();

        assertThat(edifact).isEqualTo("QTY+SVO:1750+:::mL'");
    }

    @Test
    void testFromString() {
        var edifact = "QTY+SVO:1750+:::mL'";
        var parsedFreeText = SpecimenQuantity.fromString("QTY+SVO:1750+:::mL'");
        assertThat(parsedFreeText.getQuantityUnitOfMeasure()).isEqualTo("mL");
        assertThat(parsedFreeText.getQuantity()).isEqualTo(1750);
        assertThat(parsedFreeText.toEdifact()).isEqualTo(edifact);
        assertThatThrownBy(() -> SpecimenQuantity.fromString("wrong value")).isExactlyInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testPreValidationQuantityUnitOfMeasureEmptyString() {
        SpecimenQuantity emptyFreeText = new SpecimenQuantity(1750, StringUtils.EMPTY);
        Assertions.assertThatThrownBy(emptyFreeText::preValidate)
            .isInstanceOf(EdifactValidationException.class)
            .hasMessage("QTY: Unit of measure is required");
    }

    @Test
    public void testPreValidationQuantityUnitOfMeasureBlankString() {
        SpecimenQuantity emptyFreeText = new SpecimenQuantity(1750," ");
        Assertions.assertThatThrownBy(emptyFreeText::preValidate)
            .isInstanceOf(EdifactValidationException.class)
            .hasMessage("QTY: Unit of measure is required");
    }
}
