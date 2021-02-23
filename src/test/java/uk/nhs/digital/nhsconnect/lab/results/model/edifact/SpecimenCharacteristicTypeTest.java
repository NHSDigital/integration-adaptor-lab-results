package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SpecimenCharacteristicTypeTest {

    @Test
    void testFromString() {
        var parsedFreeText = SpecimenCharacteristicType.fromString("SPC+TSP+:::BLOOD & URINE");
        assertThat(parsedFreeText.getTypeOfSpecimen()).isEqualTo("BLOOD & URINE");
    }

    @Test
    void testFromStringWithInvalidEdifactStringThrowsException() {
        assertThatThrownBy(() -> SpecimenCharacteristicType.fromString("wrong value"))
            .isExactlyInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testValidationEmptyString() {
        SpecimenCharacteristicType emptyFreeText = new SpecimenCharacteristicType(StringUtils.EMPTY);
        assertThatThrownBy(emptyFreeText::validate)
            .isInstanceOf(EdifactValidationException.class)
            .hasMessage("SPC: Attribute typeOfSpecimen is blank or missing");
    }

    @Test
    void testValidationBlankString() {
        SpecimenCharacteristicType blankFreeText = new SpecimenCharacteristicType(" ");
        assertThatThrownBy(blankFreeText::validate)
            .isInstanceOf(EdifactValidationException.class)
            .hasMessage("SPC: Attribute typeOfSpecimen is blank or missing");
    }
}
