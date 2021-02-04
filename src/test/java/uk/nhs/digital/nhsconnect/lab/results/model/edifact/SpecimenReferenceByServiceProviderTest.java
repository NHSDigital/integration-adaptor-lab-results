package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import uk.nhs.digital.nhsconnect.lab.results.model.edifact.message.EdifactValidationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SpecimenReferenceByServiceProviderTest {

    @Test
    void toEdifactTest() {
        var edifact = new SpecimenReferenceByServiceProvider("CH000064LX").toEdifact();

        assertThat(edifact).isEqualTo("RFF+STI:CH000064LX'");
    }

    @Test
    void testFromString() {
        var edifact = "RFF+STI:CH000064LX'";
        var parsedFreeText = SpecimenReferenceByServiceProvider.fromString("RFF+STI:CH000064LX'");
        assertThat(parsedFreeText.getReferenceNumber()).isEqualTo("CH000064LX");
        assertThat(parsedFreeText.toEdifact()).isEqualTo(edifact);
    }

    @Test
    void testFromStringWithInvalidEdifactStringThrowsException() {
        assertThatThrownBy(() -> SpecimenReferenceByServiceProvider.fromString("wrong value"))
            .isExactlyInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testPreValidationEmptyString() {
        SpecimenReferenceByServiceProvider emptyFreeText = new SpecimenReferenceByServiceProvider(StringUtils.EMPTY);
        assertThatThrownBy(emptyFreeText::preValidate)
            .isInstanceOf(EdifactValidationException.class)
            .hasMessage("RFF: Speciment Reference number by service provider is blank or missing");
    }

    @Test
    void testPreValidationBlankString() {
        SpecimenReferenceByServiceProvider emptyFreeText = new SpecimenReferenceByServiceProvider(" ");
        assertThatThrownBy(emptyFreeText::preValidate)
            .isInstanceOf(EdifactValidationException.class)
            .hasMessage("RFF: Speciment Reference number by service provider is blank or missing");
    }
}
