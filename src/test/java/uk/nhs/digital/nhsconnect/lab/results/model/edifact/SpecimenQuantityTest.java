package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SpecimenQuantityTest {

    private static final String SPECIMEN_QUANTITY_UNIT_OF_MEASURE = "mL";
    private static final int SPECIMEN_QUANTITY = 1750;

    @Test
    void testFromString() {
        var parsedFreeText = SpecimenQuantity.fromString("QTY+SVO:1750+:::mL");
        assertThat(parsedFreeText.getQuantityUnitOfMeasure()).isEqualTo(SPECIMEN_QUANTITY_UNIT_OF_MEASURE);
        assertThat(parsedFreeText.getQuantity()).isEqualTo(SPECIMEN_QUANTITY);
    }

    @Test
    void testFromStringWithInvalidEdifactStringThrowsException() {
        assertThatThrownBy(() -> SpecimenQuantity.fromString("wrong value"))
                .isExactlyInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testValidationQuantityUnitOfMeasureEmptyString() {
        SpecimenQuantity emptyFreeText = new SpecimenQuantity(SPECIMEN_QUANTITY, StringUtils.EMPTY);
        assertThatThrownBy(emptyFreeText::validate)
                .isInstanceOf(EdifactValidationException.class)
                .hasMessage("QTY: Unit of measure is required");
    }

    @Test
    void testValidationQuantityUnitOfMeasureBlankString() {
        SpecimenQuantity emptyFreeText = new SpecimenQuantity(SPECIMEN_QUANTITY, " ");
        assertThatThrownBy(emptyFreeText::validate)
                .isInstanceOf(EdifactValidationException.class)
                .hasMessage("QTY: Unit of measure is required");
    }
}
