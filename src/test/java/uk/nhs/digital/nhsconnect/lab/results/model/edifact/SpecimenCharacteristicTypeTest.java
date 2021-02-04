package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SpecimenCharacteristicTypeTest {

    @Test
    void toEdifactTest() {
        var edifact = new SpecimenCharacteristicType("Something").toEdifact();

        assertThat(edifact).isEqualTo("SPC+TSP+:::Something'");
    }

    @Test
    void testFromString() {
        var edifact = "SPC+TSP+:::BLOOD & URINE'";
        var parsedFreeText = SpecimenCharacteristicType.fromString("SPC+TSP+:::BLOOD & URINE'");
        assertThat(parsedFreeText.getTypeOfSpecimen()).isEqualTo("BLOOD & URINE");
        assertThat(parsedFreeText.toEdifact()).isEqualTo(edifact);
    }

    @Test
    void testFromStringWithInvalidEdifactStringThrowsException() {
        assertThatThrownBy(() -> SpecimenCharacteristicType.fromString("wrong value")).isExactlyInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testPreValidationEmptyString() {
        SpecimenCharacteristicType emptyFreeText = new SpecimenCharacteristicType(StringUtils.EMPTY);
        assertThatThrownBy(emptyFreeText::preValidate)
            .isInstanceOf(EdifactValidationException.class)
            .hasMessage("SPC: Attribute typeOfSpecimen is blank or missing");
    }

    @Test
    public void testPreValidationBlankString() {
        SpecimenCharacteristicType emptyFreeText = new SpecimenCharacteristicType(" ");
        assertThatThrownBy(emptyFreeText::preValidate)
            .isInstanceOf(EdifactValidationException.class)
            .hasMessage("SPC: Attribute typeOfSpecimen is blank or missing");
    }
}
