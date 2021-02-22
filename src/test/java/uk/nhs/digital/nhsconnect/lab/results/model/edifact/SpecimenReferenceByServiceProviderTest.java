package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SpecimenReferenceByServiceProviderTest {

    @Test
    void testFromString() {
        var parsedFreeText = SpecimenReferenceByServiceProvider.fromString("RFF+STI:CH000064LX");
        assertThat(parsedFreeText.getReferenceNumber()).isEqualTo("CH000064LX");
    }

    @Test
    void testFromStringWithInvalidEdifactStringThrowsException() {
        assertThatThrownBy(() -> SpecimenReferenceByServiceProvider.fromString("wrong value"))
            .isExactlyInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testValidationEmptyString() {
        SpecimenReferenceByServiceProvider emptyFreeText = new SpecimenReferenceByServiceProvider(StringUtils.EMPTY);
        assertThatThrownBy(emptyFreeText::validate)
            .isInstanceOf(EdifactValidationException.class)
            .hasMessage("RFF: Specimen Reference number by service provider is blank or missing");
    }

    @Test
    void testValidationBlankString() {
        SpecimenReferenceByServiceProvider emptyFreeText = new SpecimenReferenceByServiceProvider(" ");
        assertThatThrownBy(emptyFreeText::validate)
            .isInstanceOf(EdifactValidationException.class)
            .hasMessage("RFF: Specimen Reference number by service provider is blank or missing");
    }
}
