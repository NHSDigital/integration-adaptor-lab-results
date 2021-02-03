package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import org.apache.commons.lang3.StringUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class SpecimenQuantityTest {

    private static final String SPECIMEN_QUANTITY_UNIT_OF_MEASURE = "mL";
    private static final int SPECIMEN_QUANTITY = 1750;

    @Test
    void toEdifactTest() {
        var edifact = new SpecimenQuantity(SPECIMEN_QUANTITY, SPECIMEN_QUANTITY_UNIT_OF_MEASURE).toEdifact();

        assertThat(edifact).isEqualTo("QTY+SVO:1750+:::mL'");
    }

    @Test
    void testFromString() {
        var edifact = "QTY+SVO:1750+:::mL'";
        var parsedFreeText = SpecimenQuantity.fromString("QTY+SVO:1750+:::mL'");
        assertThat(parsedFreeText.getQuantityUnitOfMeasure()).isEqualTo(SPECIMEN_QUANTITY_UNIT_OF_MEASURE);
        assertThat(parsedFreeText.getQuantity()).isEqualTo(SPECIMEN_QUANTITY);
        assertThat(parsedFreeText.toEdifact()).isEqualTo(edifact);
        assertThatThrownBy(() -> SpecimenQuantity.fromString("wrong value")).isExactlyInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testPreValidationQuantityUnitOfMeasureEmptyString() {
        SpecimenQuantity emptyFreeText = new SpecimenQuantity(SPECIMEN_QUANTITY, StringUtils.EMPTY);
        Assertions.assertThatThrownBy(emptyFreeText::preValidate)
            .isInstanceOf(EdifactValidationException.class)
            .hasMessage("QTY: Unit of measure is required");
    }

    @Test
    public void testPreValidationQuantityUnitOfMeasureBlankString() {
        SpecimenQuantity emptyFreeText = new SpecimenQuantity(SPECIMEN_QUANTITY, " ");
        Assertions.assertThatThrownBy(emptyFreeText::preValidate)
            .isInstanceOf(EdifactValidationException.class)
            .hasMessage("QTY: Unit of measure is required");
    }
}
