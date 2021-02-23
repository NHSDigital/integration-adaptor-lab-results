package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SpecimenCharacteristicFastingStatusTest {

    @Test
    void testFromString() {
        var parsedFreeText = SpecimenCharacteristicFastingStatus.fromString("SPC+FS+F");
        assertThat(parsedFreeText.getFastingStatus()).isEqualTo("F");
    }

    @Test
    void testFromStringWithInvalidEdifactStringThrowsException() {
        assertThatThrownBy(() -> SpecimenCharacteristicFastingStatus.fromString("wrong value"))
            .isExactlyInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testValidationEmptyString() {
        SpecimenCharacteristicFastingStatus emptyFreeText = new SpecimenCharacteristicFastingStatus(StringUtils.EMPTY);
        assertThatThrownBy(emptyFreeText::validate)
            .isInstanceOf(EdifactValidationException.class)
            .hasMessage("SPC: Attribute fastingStatus is blank or missing");
    }

    @Test
    void testValidationBlankString() {
        SpecimenCharacteristicFastingStatus emptyFreeText = new SpecimenCharacteristicFastingStatus(" ");
        assertThatThrownBy(emptyFreeText::validate)
            .isInstanceOf(EdifactValidationException.class)
            .hasMessage("SPC: Attribute fastingStatus is blank or missing");
    }
}
